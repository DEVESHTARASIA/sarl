/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2017 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sarl.lang.ui.editor;

import com.google.inject.ImplementedBy;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

/** A service that enables to do auto-formatting when a document changed.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@ImplementedBy(DocumentAutoFormatter.class)
public interface IDocumentAutoFormatter {

	/** Create an instance of document auto formatter.
	 *
	 * @param document the Xtext document associated to this auto-formatter.
	 * @param contentFormatter the formatter of content to be used.
	 */
	default void bind(IXtextDocument document, IContentFormatter contentFormatter) {
		//
	}

	/** Start auto-formating.
	 */
	default void beginAutoFormat() {
		//
	}

	/** End auto-formating.
	 */
	default void endAutoFormat() {
		//
	}

}
