/*
 * $Id$
 *
 * File is automatically generated by the Xtext language generator.
 * Do not change it.
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright 2014-2016 the original authors and authors.
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
package io.sarl.lang.codebuilder.builders;

import io.sarl.lang.core.Behavior;
import io.sarl.lang.sarl.SarlBehavior;
import io.sarl.lang.sarl.SarlFactory;
import io.sarl.lang.sarl.SarlScript;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsFactory;
import org.eclipse.xtext.xbase.compiler.DocumentationAdapter;
import org.eclipse.xtext.xbase.lib.Pure;

/** Builder of a Sarl SarlBehavior.
 */
@SuppressWarnings("all")
public class BehaviorBuilderImpl extends AbstractBuilder implements IBehaviorBuilder {

	private SarlBehavior sarlBehavior;

	/** Initialize the Ecore element.
	 */
	public void eInit(SarlScript script, String name) {
		if (this.sarlBehavior == null) {
			this.sarlBehavior = SarlFactory.eINSTANCE.createSarlBehavior();
			script.getXtendTypes().add(this.sarlBehavior);
			if (!Strings.isEmpty(name)) {
				this.sarlBehavior.setName(name);
			}
		}
	}

	/** Replies the generated SarlBehavior.
	 */
	@Pure
	public SarlBehavior getSarlBehavior() {
		return this.sarlBehavior;
	}

	/** Replies the resource to which the SarlBehavior is attached.
	 */
	@Pure
	public Resource eResource() {
		return getSarlBehavior().eResource();
	}

	/** Change the documentation of the element.
	 *
	 * <p>The documentation will be displayed just before the element.
	 *
	 * @param doc the documentation.
	 */
	public void setDocumentation(String doc) {
		if (Strings.isEmpty(doc)) {
			getSarlBehavior().eAdapters().removeIf(new Predicate<Adapter>() {
				public boolean test(Adapter adapter) {
					return adapter.isAdapterForType(DocumentationAdapter.class);
				}
			});
		} else {
			DocumentationAdapter adapter = (DocumentationAdapter) EcoreUtil.getExistingAdapter(
					getSarlBehavior(), DocumentationAdapter.class);
			if (adapter == null) {
				adapter = new DocumentationAdapter();
				getSarlBehavior().eAdapters().add(adapter);
			}
			adapter.setDocumentation(doc);
		}
	}

	/** Change the super type.
	 * @param superType - the qualified name of the super type,
	 *     or <code>null</code> if the default type.
	 */
	public void setExtends(String superType) {
		if (!Strings.isEmpty(superType)
				&& !Behavior.class.getName().equals(superType)) {
			JvmParameterizedTypeReference superTypeRef = newTypeRef(this.sarlBehavior, superType);
			JvmTypeReference baseTypeRef = newTypeRef(this.sarlBehavior, Behavior.class);
			if (isSubTypeOf(this.sarlBehavior, superTypeRef, baseTypeRef)) {
				this.sarlBehavior.setExtends(superTypeRef);
				return;
			}
		}
		this.sarlBehavior.setExtends(null);
	}

	/** Add an annotation.
	 * @param type - the qualified name of the annotation.
	 */
	public void addAnnotation(String type) {
		if (!Strings.isEmpty(type)) {
			XAnnotation annotation = XAnnotationsFactory.eINSTANCE.createXAnnotation();
			annotation.setAnnotationType(newTypeRef(sarlBehavior, type).getType());
			this.sarlBehavior.getAnnotations().add(annotation);
		}
	}

	/** Add a modifier.
	 * @param modifier - the modifier to add.
	 */
	public void addModifier(String modifier) {
		if (!Strings.isEmpty(modifier)) {
			this.sarlBehavior.getModifiers().add(modifier);
		}
	}

	@Inject
	private Provider<IConstructorBuilder> iConstructorBuilderProvider;

	/** Create a Constructor.
	 * @return the builder.
	 */
	public IConstructorBuilder addConstructor() {
		IConstructorBuilder builder = this.iConstructorBuilderProvider.get();
		builder.eInit(getSarlBehavior());
		return builder;
	}

	@Inject
	private Provider<IActionBuilder> iActionBuilderProvider;

	/** Create an Action.
	 * @param name - the name of the Action.
	 * @return the builder.
	 */
	public IActionBuilder addAction(String name) {
		IActionBuilder builder = this.iActionBuilderProvider.get();
		builder.eInit(getSarlBehavior(), name);
		return builder;
	}

	@Inject
	private Provider<IBehaviorUnitBuilder> iBehaviorUnitBuilderProvider;

	/** Create a BehaviorUnit.
	 * @param name - the typename of the BehaviorUnit.
	 * @return the builder.
	 */
	public IBehaviorUnitBuilder addBehaviorUnit(String name) {
		IBehaviorUnitBuilder builder = this.iBehaviorUnitBuilderProvider.get();
		builder.eInit(getSarlBehavior(), name);
		return builder;
	}

	@Inject
	private Provider<IFieldBuilder> iFieldBuilderProvider;

	/** Create a Field.
	 * @param name - the name of the Field.
	 * @return the builder.
	 */
	public IFieldBuilder addVarField(String name) {
		IFieldBuilder builder = this.iFieldBuilderProvider.get();
		builder.eInit(getSarlBehavior(), name, "var");
		return builder;
	}

	/** Create a Field.
	 * @param name - the name of the Field.
	 * @return the builder.
	 */
	public IFieldBuilder addValField(String name) {
		IFieldBuilder builder = this.iFieldBuilderProvider.get();
		builder.eInit(getSarlBehavior(), name, "val");
		return builder;
	}

	/** Create a Field.	 *
	 * <p>This function is equivalent to {@link #addVarField}.
	 * @param name - the name of the Field.
	 * @return the builder.
	 */
	public IFieldBuilder addField(String name) {
		return this.addVarField(name);
	}

}
