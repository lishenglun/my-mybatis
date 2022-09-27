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
package org.apache.ibatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拦截器𥰁
 *
 * @author Clinton Begin
 */
public class InterceptorChain {

  // 拦截器集合
  // 主要：⚠️如果有多个拦截器，就会生成一条动态代理对象链，就是代理对象嵌套着动态代理对象！
  private final List<Interceptor> interceptors = new ArrayList<>();

  /**
   * 对目标对象应用插件，说白了就是：如果目标对象实现的接口中，是要被拦截的接口，则对目标对象创建动态代理
   *
   * @param target      目标对象（应用插件的对象）
   */
  public Object pluginAll(Object target) {
    // 遍历拦截器集合
    // 主要：⚠️如果有多个拦截器，就会生成一条动态代理对象链，就是代理对象嵌套着动态代理对象！
    for (Interceptor interceptor : interceptors) {
      // 调用Interceptor#plugin()，内部就是对目标对象进行动态代理
      target = interceptor.plugin(target);
    }

    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }

  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
