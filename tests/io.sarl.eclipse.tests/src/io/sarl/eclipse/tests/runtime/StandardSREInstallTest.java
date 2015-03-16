/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2015 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
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
package io.sarl.eclipse.tests.runtime;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import foo.Foo;
import io.sarl.eclipse.SARLEclipsePlugin;
import io.sarl.eclipse.runtime.SARLRuntime;
import io.sarl.eclipse.runtime.StandardSREInstall;
import io.sarl.tests.api.AbstractSarlTest;
import io.sarl.tests.api.AbstractSarlUiTest;
import io.sarl.tests.api.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.launching.LibraryLocation;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.io.Resources;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	StandardSREInstallTest.Valid.class,
	StandardSREInstallTest.Invalid.class,
})
@SuppressWarnings("all")
public class StandardSREInstallTest {

	public static final String TESTING_PREFERENCE_KEY = SARLEclipsePlugin.PLUGIN_ID + ".tests.runtime.PREF_SRE_XML"; //$NON-NLS-1$

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class Valid extends AbstractSarlUiTest {

		@Nullable
		private SARLEclipsePlugin plugin;

		@Nullable
		private IEclipsePreferences preferences;

		@Nullable
		private BundleContext bundleContext;

		@Nullable
		private Bundle bundle;

		@Nullable
		private URL jarFile;

		@Nullable
		private String id;

		@Nullable
		private StandardSREInstall sre;

		@Nullable
		private IPath path;

		@Before
		public void setUp() throws Exception {
			this.preferences = new EclipsePreferences();
			this.plugin = spy(new SARLEclipsePlugin());
			SARLEclipsePlugin.setDefault(this.plugin);
			when(this.plugin.getPreferences()).thenReturn(this.preferences);
			this.bundle = mock(Bundle.class);
			when(this.bundle.getSymbolicName()).thenReturn(SARLEclipsePlugin.PLUGIN_ID);
			this.bundleContext = mock(BundleContext.class);
			when(this.bundleContext.getBundle()).thenReturn(this.bundle);
			when(this.bundle.getBundleContext()).thenReturn(this.bundleContext);
			SARLRuntime.setCurrentPreferenceKey(TESTING_PREFERENCE_KEY);
			this.plugin.start(this.bundleContext);
			//
			this.jarFile = Resources.getResource(Foo.class, "/foo/foo.jar");
			Assume.assumeNotNull(this.jarFile);
			this.id = UUID.randomUUID().toString();
			this.sre = new StandardSREInstall(this.id);
			this.jarFile = FileLocator.toFileURL(this.jarFile);
			URI uri = this.jarFile.toURI();
			this.path = URIUtil.toPath(uri);
			this.sre.setJarFile(this.path);
		}

		@Test
		public void getJarFile() {
			assertSame(this.path, this.sre.getJarFile());
		}

		@Test
		public void getNameNoDefault() {
			assertEquals("My SRE", this.sre.getNameNoDefault());
		}

		@Test
		public void getName() {
			assertEquals("My SRE", this.sre.getName());
		}

		@Test
		public void getSREArguments() {
			assertEquals("fghi jkl", this.sre.getSREArguments());
		}

		@Test
		public void getVMArguments() {
			assertEquals("abc de", this.sre.getJVMArguments());
		}

		@Test
		public void getLibraryLocation() {
			assertArrayEquals(new LibraryLocation[] {
					new LibraryLocation(this.path, Path.EMPTY, Path.EMPTY),
					new LibraryLocation(Path.fromPortableString("a.jar"), Path.EMPTY, Path.EMPTY),
					new LibraryLocation(Path.fromPortableString("b.jar"), Path.EMPTY, Path.EMPTY),
					new LibraryLocation(Path.fromPortableString("c.jar"), Path.EMPTY, Path.EMPTY),
			}, this.sre.getLibraryLocations());
		}

		@Test
		public void getLocation() {
			assertEquals(this.path.toOSString(), this.sre.getLocation());
		}

		@Test
		public void getAsXML() throws Exception {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmldocument = builder.newDocument();
			Element root = xmldocument.createElement("root"); //$NON-NLS-1$
			this.sre.getAsXML(xmldocument, root);
			xmldocument.appendChild(root);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer trans = transFactory.newTransformer();
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				DOMSource source = new DOMSource(xmldocument);
				PrintWriter flot = new PrintWriter(baos);
				StreamResult xmlStream = new StreamResult(flot);
				trans.transform(source, xmlStream);
				String content = new String(baos.toByteArray());
				String[] expected = new String[] {
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
						"<root libraryPath=\"" + this.path.toOSString() + "\" standalone=\"true\">",
						"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"" + this.path.toOSString() + "\"/>",
						"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"a.jar\"/>",
						"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"b.jar\"/>",
						"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"c.jar\"/>",
						"</root>",
				};
				StringBuilder b = new StringBuilder();
				for(String s : expected) {
					b.append(s);
					//b.append("\n");
				}
				assertEquals(b.toString(), content);
			}
		}

		@Test
		public void setFromXML() throws Exception {
			String[] expected = new String[] {
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
					"<SRE name=\"Hello\" mainClass=\"io.sarl.Boot\" libraryPath=\"" + this.path.toOSString() + "\" standalone=\"true\">",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"" + this.path.toOSString() + "\"/>",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"x.jar\"/>",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"y.jar\"/>",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"z.jar\"/>",
					"</SRE>",
			};
			StringBuilder b = new StringBuilder();
			for(String s : expected) {
				b.append(s);
				//b.append("\n");
			}
			try (ByteArrayInputStream bais = new ByteArrayInputStream(b.toString().getBytes())) {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				parser.setErrorHandler(new DefaultHandler());
				Element root = parser.parse(new InputSource(bais)).getDocumentElement();
				this.sre.setFromXML(root);
				assertTrue(this.sre.isStandalone());
				assertEquals(this.path, this.sre.getJarFile());
				assertEquals("Hello", this.sre.getName());
				assertEquals("io.sarl.Boot", this.sre.getMainClass());
			}
		}

		@Test
		public void testClone() {
			StandardSREInstall c = this.sre.clone();
			assertNotNull(c);
			assertSame(this.id, c.getId());
			assertEquals(this.sre.getName(), c.getName());
			assertEquals(this.sre.getMainClass(), c.getMainClass());
			assertEquals(this.sre.getMaximalSARLVersion(), c.getMaximalSARLVersion());
			assertEquals(this.sre.getMinimalSARLVersion(), c.getMinimalSARLVersion());
			assertEquals(this.sre.getLocation(), c.getLocation());
			assertEquals(this.sre.getSREArguments(), c.getSREArguments());
			assertEquals(this.sre.getJVMArguments(), c.getJVMArguments());
			assertEquals(this.sre.getJarFile(), c.getJarFile());
			assertArrayEquals(this.sre.getLibraryLocations(), c.getLibraryLocations());
		}

		@Test
		public void copy() {
			String id = UUID.randomUUID().toString();
			StandardSREInstall c = this.sre.copy(id);
			assertNotNull(c);
			assertSame(id, c.getId());
			assertEquals(this.sre.getName(), c.getName());
			assertEquals(this.sre.getMainClass(), c.getMainClass());
			assertEquals(this.sre.getMaximalSARLVersion(), c.getMaximalSARLVersion());
			assertEquals(this.sre.getMinimalSARLVersion(), c.getMinimalSARLVersion());
			assertEquals(this.sre.getLocation(), c.getLocation());
			assertEquals(this.sre.getSREArguments(), c.getSREArguments());
			assertEquals(this.sre.getJVMArguments(), c.getJVMArguments());
			assertEquals(this.sre.getJarFile(), c.getJarFile());
			assertArrayEquals(this.sre.getLibraryLocations(), c.getLibraryLocations());
		}

		@Test
		public void getValidityInt() {
			IStatus s = this.sre.getValidity();
			assertNotNull(s);
			assertTrue(s.isOK());
		}

		@Test
		public void revalidate() {
			this.sre.revalidate();
		}

		@Test
		public void getValidity() {
			IStatus s = this.sre.getValidity();
			assertNotNull(s);
			assertTrue(s.isOK());
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class Invalid extends AbstractSarlUiTest {

		@Nullable
		private SARLEclipsePlugin plugin;

		@Nullable
		private IEclipsePreferences preferences;

		@Nullable
		private BundleContext bundleContext;

		@Nullable
		private Bundle bundle;

		@Nullable
		private URL jarFile;

		@Nullable
		private String id;

		@Nullable 
		private StandardSREInstall sre;

		@Nullable
		private IPath path;

		@Before
		public void setUp() throws Exception {
			this.preferences = new EclipsePreferences();
			this.plugin = spy(new SARLEclipsePlugin());
			SARLEclipsePlugin.setDefault(this.plugin);
			when(this.plugin.getPreferences()).thenReturn(this.preferences);
			this.bundle = mock(Bundle.class);
			when(this.bundle.getSymbolicName()).thenReturn(SARLEclipsePlugin.PLUGIN_ID);
			this.bundleContext = mock(BundleContext.class);
			when(this.bundleContext.getBundle()).thenReturn(this.bundle);
			when(this.bundle.getBundleContext()).thenReturn(this.bundleContext);
			SARLRuntime.setCurrentPreferenceKey(TESTING_PREFERENCE_KEY);
			this.plugin.start(this.bundleContext);
			//
			this.jarFile = Resources.getResource(Foo.class, "/foo/foo2.jar");
			Assume.assumeNotNull(this.jarFile);
			this.id = UUID.randomUUID().toString();
			this.sre = new StandardSREInstall(this.id);
			this.jarFile = FileLocator.toFileURL(this.jarFile);
			URI uri = this.jarFile.toURI();
			this.path = URIUtil.toPath(uri);
			this.sre.setJarFile(this.path);
		}

		@Test
		public void getJarFile() {
			assertSame(this.path, this.sre.getJarFile());
		}

		@Test
		public void getNameNoDefault() {
			assertEquals("My SRE 2", this.sre.getNameNoDefault());
		}

		@Test
		public void getName() {
			assertEquals("My SRE 2", this.sre.getName());
		}

		@Test
		public void getProgramArguments() {
			assertEquals("", this.sre.getSREArguments());
		}

		@Test
		public void getVMArguments() {
			assertEquals("", this.sre.getJVMArguments());
		}

		@Test
		public void getLibraryLocation() {
			assertArrayEquals(new LibraryLocation[] {
					new LibraryLocation(this.path, Path.EMPTY, Path.EMPTY),
			}, this.sre.getLibraryLocations());
		}

		@Test
		public void getLocation() {
			assertEquals(this.path.toOSString(), this.sre.getLocation());
		}

		@Test
		public void getAsXML() throws Exception {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmldocument = builder.newDocument();
			Element root = xmldocument.createElement("root"); //$NON-NLS-1$
			this.sre.getAsXML(xmldocument, root);
			xmldocument.appendChild(root);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer trans = transFactory.newTransformer();
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				DOMSource source = new DOMSource(xmldocument);
				PrintWriter flot = new PrintWriter(baos);
				StreamResult xmlStream = new StreamResult(flot);
				trans.transform(source, xmlStream);
				String content = new String(baos.toByteArray());
				String[] expected = new String[] {
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
						"<root libraryPath=\"" + this.path.toOSString() + "\" standalone=\"false\"/>",
				};
				StringBuilder b = new StringBuilder();
				for(String s : expected) {
					b.append(s);
					//b.append("\n");
				}
				assertEquals(b.toString(), content);
			}
		}

		@Test
		public void setFromXML() throws Exception {
			String[] expected = new String[] {
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
					"<SRE name=\"Hello\" mainClass=\"io.sarl.Boot\" libraryPath=\"" + this.path.toOSString() + "\" standalone=\"true\">",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"" + this.path.toOSString() + "\"/>",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"x.jar\"/>",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"y.jar\"/>",
					"<libraryLocation packageRootPath=\"\" sourcePath=\"\" systemLibraryPath=\"z.jar\"/>",
					"</SRE>",
			};
			StringBuilder b = new StringBuilder();
			for(String s : expected) {
				b.append(s);
				//b.append("\n");
			}
			try (ByteArrayInputStream bais = new ByteArrayInputStream(b.toString().getBytes())) {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				parser.setErrorHandler(new DefaultHandler());
				Element root = parser.parse(new InputSource(bais)).getDocumentElement();
				this.sre.setFromXML(root);
				assertTrue(this.sre.isStandalone());
				assertEquals(this.path, this.sre.getJarFile());
				assertEquals("Hello", this.sre.getName());
				assertEquals("io.sarl.Boot", this.sre.getMainClass());
			}
		}

		@Test
		public void testClone() {
			StandardSREInstall c = this.sre.clone();
			assertNotNull(c);
			assertSame(this.id, c.getId());
			assertEquals(this.sre.getName(), c.getName());
			assertEquals(this.sre.getMainClass(), c.getMainClass());
			assertEquals(this.sre.getMaximalSARLVersion(), c.getMaximalSARLVersion());
			assertEquals(this.sre.getMinimalSARLVersion(), c.getMinimalSARLVersion());
			assertEquals(this.sre.getLocation(), c.getLocation());
			assertEquals(this.sre.getSREArguments(), c.getSREArguments());
			assertEquals(this.sre.getJVMArguments(), c.getJVMArguments());
			assertEquals(this.sre.getJarFile(), c.getJarFile());
			assertArrayEquals(this.sre.getLibraryLocations(), c.getLibraryLocations());
		}

		@Test
		public void copy() {
			String id = UUID.randomUUID().toString();
			StandardSREInstall c = this.sre.copy(id);
			assertNotNull(c);
			assertSame(id, c.getId());
			assertEquals(this.sre.getName(), c.getName());
			assertEquals(this.sre.getMainClass(), c.getMainClass());
			assertEquals(this.sre.getMaximalSARLVersion(), c.getMaximalSARLVersion());
			assertEquals(this.sre.getMinimalSARLVersion(), c.getMinimalSARLVersion());
			assertEquals(this.sre.getLocation(), c.getLocation());
			assertEquals(this.sre.getSREArguments(), c.getSREArguments());
			assertEquals(this.sre.getJVMArguments(), c.getJVMArguments());
			assertEquals(this.sre.getJarFile(), c.getJarFile());
			assertArrayEquals(this.sre.getLibraryLocations(), c.getLibraryLocations());
		}

		@Test
		public void getValidityInt() {
			IStatus s = this.sre.getValidity();
			assertNotNull(s);
			assertFalse(s.isOK());
			assertEquals(64, s.getCode());
		}

		@Test
		public void revalidate() {
			this.sre.revalidate();
		}

		@Test
		public void getValidity() {
			IStatus s = this.sre.getValidity();
			assertNotNull(s);
			assertFalse(s.isOK());
			assertEquals(64, s.getCode());
		}

	}

}
