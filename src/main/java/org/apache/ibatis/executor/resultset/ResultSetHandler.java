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
package org.apache.ibatis.executor.resultset;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;

/**
 * 结果集处理器
 *
 * @author Clinton Begin
 */
public interface ResultSetHandler {

  /* ResultSet：结果集 */

  // 处理ResultSet，转化成相应的结果对象集合 —— 将结果集转化成list
  <E> List<E> handleResultSets(Statement stmt) throws SQLException;

  // 处理ResultSet，转化成相应的一个游标对象
  <E> Cursor/* 游标 */<E> handleCursorResultSets(Statement stmt) throws SQLException;

  // 处理存储过程的输出参数
  void handleOutputParameters(CallableStatement cs) throws SQLException;

}
