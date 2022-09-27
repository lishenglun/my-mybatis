/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.CollectionWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * 与BeanWrapper的意思差不多，都是一个包装器，来帮我们进行具体的操作。
 *
 * 包含了属性的操作
 *
 * @author Clinton Begin
 */
public class MetaObject {

  // 原始javaBean对象
  private final Object originalObject;
  // 对象包裝器
  private final ObjectWrapper objectWrapper;
  // 负责实例化originalObject的工厂对象
  private final ObjectFactory objectFactory;
  // 负责创建ObjectWrapper的工厂对象
  private final ObjectWrapperFactory objectWrapperFactory;
  // 用于创建并缓存Reflector对象的工厂对象
  private final ReflectorFactory reflectorFactory;

  private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.originalObject = object;
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;

    // 对象包装器
    if (object instanceof ObjectWrapper) {
      // 如果对象本身已经是ObjectWrapper型，则直接赋给objectWrapper
      this.objectWrapper = (ObjectWrapper) object;
    }
    // 对象包装工厂
    else if (objectWrapperFactory.hasWrapperFor(object)) {
      // 如果有包装器,调用ObjectWrapperFactory.getWrapperFor
      this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
    }
    // Map
    else if (object instanceof Map) {
      // 如果是Map型，返回MapWrapper
      this.objectWrapper = new MapWrapper(this, (Map) object);
    }
    // Collection
    else if (object instanceof Collection) {
      // 如果是Collection型，返回CollectionWrapper
      this.objectWrapper = new CollectionWrapper(this, (Collection) object);
    } else {
      // 除此以外，返回BeanWrapper
      this.objectWrapper = new BeanWrapper(this, object);
    }
  }

  // MetaObject的构造方法是private修饰的：只能通过forObject这个静态方法创建MetaObject对象
  public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
      // 处理一下null,将null包装起来
      // 若object为nulL，则统一返回SystemMetaObject.NULL_META_OBJECT
      return SystemMetaObject.NULL_META_OBJECT;
    } else {
      // ⚠️
      return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  public ReflectorFactory getReflectorFactory() {
    return reflectorFactory;
  }

  public Object getOriginalObject() {
    return originalObject;
  }

  //--------以下方法都是委派给ObjectWrapper------

  // 查找属性
  public String findProperty(String propName, boolean useCamelCaseMapping) {
    return objectWrapper.findProperty(propName, useCamelCaseMapping);
  }

  // 取得getter的名字列表
  public String[] getGetterNames() {
    return objectWrapper.getGetterNames();
  }

  // 取得setter的名字列表
  public String[] getSetterNames() {
    return objectWrapper.getSetterNames();
  }

  // 取得setter的类型列表
  public Class<?> getSetterType(String name) {
    return objectWrapper.getSetterType(name);
  }

  // 取得getter的类型列表
  public Class<?> getGetterType(String name) {
    return objectWrapper.getGetterType(name);
  }

  // 是否有指定的setter
  public boolean hasSetter(String name) {
    return objectWrapper.hasSetter(name);
  }

  // 是否有指定的getter
  public boolean hasGetter(String name) {
    return objectWrapper.hasGetter(name);
  }

  /**
   * 从某个对象中获取属性值，例如：
   * <p>
   * User user = new User();
   * MetaObject metaObject = SystemMetaObject.forObject(user);
   * metaObject.setValue("userField","lian");
   * metaObject.getValue("userField");
   * <p>
   * 就是从user对象里面获取userField属性（字段）的属性值，得到是lian
   * <p>
   * 这样的写法也可以：
   * metaObject.setValue("user.userField","lian");
   * metaObject.getValue("user.userField");
   * 往map类型的属性里面取出值
   * metaObject.setValue("myMap[key]","lian");
   * metaObject.getValue("myMap[key]");
   * <p>
   * @param name  属性名
   */
  //取得值
  //如person[0].birthdate.year
  //具体测试用例可以看MetaObjectTest
  public Object getValue(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        //如果上层就是null了，那就结束，返回null
        return null;
      } else {
        //否则继续看下一层，递归调用getValue
        return metaValue.getValue(prop.getChildren());
      }
    } else {
      // 通过ObjectWrapper获取指定的属性值
      return objectWrapper.get(prop);
    }
  }

  /**
   * 为某个对象设置属性值，例如：
   * <p>
   * User user = new User();
   * MetaObject metaObject = SystemMetaObject.forObject(user);
   * metaObject.setValue("userField","lian");
   * <p>
   * 就是往user对象里面设置userField属性（字段）的属性值为lian，
   * <p>
   * 这样的写法也可以：
   * metaObject.setValue("user.userField","lian");
   * 往map类型的属性里面放入值
   * metaObject.setValue("myMap[key]","lian");
   * <p>
   * @param name  属性名
   * @param value 属性值
   */
  //设置值
  //如person[0].birthdate.year
  public void setValue(String name/* 属性名 */, Object value/* 属性值 */) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        if (value == null) {
          // don't instantiate child path if value is null
          //如果上层就是null了，还得看有没有儿子，没有那就结束
          return;
        } else {
          //否则还得new一个，委派给ObjectWrapper.instantiatePropertyValue
          metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
        }
      }
      //递归调用setValue
      metaValue.setValue(prop.getChildren(), value);
    } else {
      // ⚠️到了最后一层了，所以委派给ObjectWrapper.set
      objectWrapper.set(prop, value);
    }
  }

  //为某个属性生成元对象
  public MetaObject metaObjectForProperty(String name) {
    //实际是递归调用
    Object value = getValue(name);
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  public ObjectWrapper getObjectWrapper() {
    return objectWrapper;
  }

  //是否是集合
  public boolean isCollection() {
    return objectWrapper.isCollection();
  }

  //添加属性
  public void add(Object element) {
    objectWrapper.add(element);
  }

  //添加属性
  public <E> void addAll(List<E> list) {
    objectWrapper.addAll(list);
  }

}
