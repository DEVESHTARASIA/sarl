/*
 * Copyright (C) 2014-2018 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sarl.lang.tests.bugs.to00699;

import java.awt.event.WindowEvent;

import com.google.inject.Inject;
import org.eclipse.xtend.core.validation.IssueCodes;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.testing.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.sarl.lang.SARLVersion;
import io.sarl.lang.sarl.SarlPackage;
import io.sarl.lang.sarl.SarlScript;
import io.sarl.tests.api.AbstractSarlTest;

/** Testing class for issue: Invalid anonymous class expression.
 *
 * <p>https://github.com/sarl/sarl/issues/599
 *
 * @author $Author: sgalland$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class Bug599 extends AbstractSarlTest {

	private static final String SNIPSET1 = multilineString(
			"package io.sarl.lang.tests.bug599",
			"import java.awt.^event.MouseMotionListener",
			"import java.awt.^event.MouseEvent",
			"class XXX {",
			"  new {",
			"    new MouseMotionListener {",
			"      override mouseDragged(e : MouseEvent) {",
			"      }",
			"      override mouseMoved(e : MouseEvent) {",
			"      }",
			"    }",
			"  }",
			"}");

	private static final String SNIPSET2 = multilineString(
			"package io.sarl.lang.tests.bug599",
			"import java.awt.^event.WindowAdapter",
			"import java.awt.^event.WindowEvent",
			"class XXX {",
			"  new {",
			"    new WindowAdapter {",
			"      override windowClosed(e : WindowEvent) {",
			"      }",
			"    }",
			"  }",
			"}");

	@Inject
	private CompilationTestHelper compiler;

	@Test
	public void parsing_01() throws Exception {
		SarlScript mas = file(SNIPSET1);
		final Validator validator = validate(mas);
		validator.assertNoErrors();
	}

	@Test
	public void compiling_01() throws Exception {
		this.compiler.assertCompilesTo(SNIPSET1, multilineString(
				"package io.sarl.lang.tests.bug599;",
				"",
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import java.awt.event.MouseEvent;",
				"import java.awt.event.MouseMotionListener;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class XXX {",
				"  public XXX() {",
				"    new MouseMotionListener() {",
				"      @Override",
				"      public void mouseDragged(final MouseEvent e) {",
				"      }",
				"      ",
				"      @Override",
				"      public void mouseMoved(final MouseEvent e) {",
				"      }",
				"    };",
				"  }",
				"}",
				""));
	}

	@Test
	public void parsing_02() throws Exception {
		SarlScript mas = file(SNIPSET2);
		final Validator validator = validate(mas);
		validator.assertNoErrors();
	}

	@Test
	public void compiling_02() throws Exception {
		this.compiler.assertCompilesTo(SNIPSET2, multilineString(
				"package io.sarl.lang.tests.bug599;",
				"",
				"import io.sarl.lang.annotation.SarlElementType;",
				"import io.sarl.lang.annotation.SarlSpecification;",
				"import java.awt.event.WindowAdapter;",
				"import java.awt.event.WindowEvent;",
				"",
				"@SarlSpecification(\"" + SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + "\")",
				"@SarlElementType(" + SarlPackage.SARL_CLASS + ")",
				"@SuppressWarnings(\"all\")",
				"public class XXX {",
				"  public XXX() {",
				"    new WindowAdapter() {",
				"      @Override",
				"      public void windowClosed(final WindowEvent e) {",
				"      }",
				"    };",
				"  }",
				"}",
				""));
	}

}
