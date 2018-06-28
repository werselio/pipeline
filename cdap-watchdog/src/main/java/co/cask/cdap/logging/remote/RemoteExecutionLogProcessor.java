/*
 * Copyright © 2018 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.logging.remote;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Interface defining how to process logs from coming from a remote program execution.
 */
public interface RemoteExecutionLogProcessor {

  /**
   * Processes the message.
   *
   * @param loggingEvent the event coming from the remote execution
   */
  void process(ILoggingEvent loggingEvent);
}
