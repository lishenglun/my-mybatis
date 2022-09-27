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
package org.apache.ibatis.mapping;

import javax.sql.DataSource;

import org.apache.ibatis.transaction.TransactionFactory;

/**
 * @author Clinton Begin
 */
public final class Environment {


  /**
   * 例如：id = development
   *
   * <environments default="development">
   *     <environment id="development">
   *       <transactionManager type="JDBC"/>
   *       <dataSource type="POOLED">
   *         <property name="driver" value="${jdbc.driver}"/>
   *         <property name="url" value="${jdbc.url}"/>
   *         <property name="username" value="${jdbc.username}"/>
   *         <property name="password" value="${jdbc.password}"/>
   *       </dataSource>
   *     </environment>
   * </environments>
   */
  // 数据库环境id
  private final String id;
  // 事务工厂（事务管理器）
  private final TransactionFactory transactionFactory;
  // 数据源
  private final DataSource dataSource;

  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
    if (id == null) {
      throw new IllegalArgumentException("Parameter 'id' must not be null");
    }
    if (transactionFactory == null) {
      throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
    }
    this.id = id;
    if (dataSource == null) {
      throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
    }
    this.transactionFactory = transactionFactory;
    this.dataSource = dataSource;
  }

  public static class Builder {

    // 环境id
    // 题外：在单独使用mybatis的时候，由于只加载开发环境的数据库信息，所以id一定等于development
    private final String id;

    // 事务工厂（事务管理器）
    private TransactionFactory transactionFactory;

    // 数据源
    private DataSource dataSource;

    public Builder(String id) {
      this.id = id;
    }

    public Builder transactionFactory(TransactionFactory transactionFactory) {
      this.transactionFactory = transactionFactory;
      return this;
    }

    public Builder dataSource(DataSource dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    public String id() {
      return this.id;
    }

    public Environment build() {
      return new Environment(this.id, this.transactionFactory, this.dataSource);
    }

  }

  public String getId() {
    return this.id;
  }

  public TransactionFactory getTransactionFactory() {
    return this.transactionFactory;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

}
