/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.kubesys.client.exceptions;


/**
 * @author  wuheng@iscas.ac.cn
 * @since   2023/07/25
 * @version 1.0.0
 *
 */
public class KubernetesBadRequestException extends RuntimeException {

	/**
	 * uid
	 */
	private static final long serialVersionUID = 3209894198735325182L;


	/**
	 * @param message messsage
	 */
	public KubernetesBadRequestException(String message) {
		super(message);
	}

}
