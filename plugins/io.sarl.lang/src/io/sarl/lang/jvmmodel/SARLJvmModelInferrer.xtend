/*
 * Copyright 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
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
package io.sarl.lang.jvmmodel

import com.google.inject.Inject
import io.sarl.lang.SARLKeywords
import io.sarl.lang.annotation.DefaultValue
import io.sarl.lang.annotation.DefaultValueSource
import io.sarl.lang.annotation.DefaultValueUse
import io.sarl.lang.annotation.Generated
import io.sarl.lang.bugfixes.XtendBug392440
import io.sarl.lang.bugfixes.XtendBug434912
import io.sarl.lang.core.Address
import io.sarl.lang.core.Percept
import io.sarl.lang.sarl.Action
import io.sarl.lang.sarl.ActionSignature
import io.sarl.lang.sarl.Agent
import io.sarl.lang.sarl.Attribute
import io.sarl.lang.sarl.Behavior
import io.sarl.lang.sarl.BehaviorUnit
import io.sarl.lang.sarl.Capacity
import io.sarl.lang.sarl.CapacityUses
import io.sarl.lang.sarl.Constructor
import io.sarl.lang.sarl.Event
import io.sarl.lang.sarl.FormalParameter
import io.sarl.lang.sarl.ImplementingElement
import io.sarl.lang.sarl.InheritingElement
import io.sarl.lang.sarl.RequiredCapacity
import io.sarl.lang.sarl.Skill
import io.sarl.lang.sarl.TopElement
import io.sarl.lang.signature.ActionKey
import io.sarl.lang.signature.ActionSignatureProvider
import io.sarl.lang.signature.InferredStandardParameter
import io.sarl.lang.signature.InferredValuedParameter
import io.sarl.lang.signature.SignatureKey
import java.util.UUID
import java.util.logging.Logger
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmExecutable
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor.IPostIndexingInitializing
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.typesystem.util.CommonTypeComputationServices
import org.eclipse.xtext.xbase.validation.ReadAndWriteTracking

import static io.sarl.lang.util.ModelUtil.*
import org.eclipse.xtext.xbase.XBooleanLiteral
import java.util.List
import java.util.Map

/**
 * <p>Infers a JVM model from the source model.</p> 
 *
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against 
 * the JVM model rather than the source model.</p>
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class SARLJvmModelInferrer extends AbstractModelInferrer {

	@Inject extension JvmTypesBuilder

	@Inject extension IQualifiedNameProvider
	
	@Inject private XbaseCompiler xbaseCompiler

	@Inject private JvmModelAssociator jvmModelAssociator

	@Inject private Logger log

	@Inject private ActionSignatureProvider sarlSignatureProvider

	@Inject	private ReadAndWriteTracking readAndWriteTracking

	@Inject	private CommonTypeComputationServices services

	private var XtendBug392440 hashCodeBugFix
	private var XtendBug434912 toEqualsBugFix
	
	@Inject
	def void setTypesBuilder(JvmTypesBuilder typesBuilder) {
		this.hashCodeBugFix = new XtendBug392440(typesBuilder)
		this.toEqualsBugFix = new XtendBug434912(typesBuilder)
	}

	/**
	 * The dispatch method {@code infer} is called for each instance of the
	 * given element's type that is contained in a resource.
	 * 
	 * @param element
	 *            the model to create one or more
	 *            {@link JvmDeclaredType declared
	 *            types} from.
	 * @param acceptor
	 *            each created
	 *            {@code type}
	 *            without a container should be passed to the acceptor in order
	 *            get attached to the current resource. The acceptor's
	 *            {@link IJvmDeclaredTypeAcceptor#accept(org.eclipse.xtext.common.types.JvmDeclaredType)
	 *            accept(..)} method takes the constructed empty type for the
	 *            pre-indexing phase. This one is further initialized in the
	 *            indexing phase using the closure you pass to the returned
	 *            {@link IPostIndexingInitializing#initializeLater(org.eclipse.xtext.xbase.lib.Procedures.Procedure1)
	 *            initializeLater(..)}.
	 * @param isPreIndexingPhase
	 *            whether the method is called in a pre-indexing phase, i.e.
	 *            when the global index is not yet fully updated. You must not
	 *            rely on linking using the index if isPreIndexingPhase is
	 *            <code>true</code>.
	 */
	def dispatch void infer(Event event, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(event.toClass(event.fullyQualifiedName)).initializeLater(
			[
				// Reset the action registry
				sarlSignatureProvider.resetSignatures(it)

				event.copyDocumentationTo(it)
				
				var long serial = 1L
				serial = serial + generateExtendedTypes(it, event, io.sarl.lang.core.Event)
				var JvmField jvmField
				var jvmFields = newArrayList
				var actionIndex = 0
				var hasConstructor = false

				for (feature : event.features) {
					if (feature!==null) {
						switch feature {
							Attribute: {
								jvmField = generateAttribute(feature, JvmVisibility::PUBLIC)
								if (jvmField!==null) {
									jvmFields.add(jvmField)
									members += jvmField
									serial = serial + feature.name.hashCode
								}
							}
							Constructor: {
								if (generateConstructor(event, feature, actionIndex) !== null) {
									serial = serial + event.fullyQualifiedName.hashCode
									actionIndex++
									hasConstructor = true
								}
							}
						}
					}
				}
				
				if (!hasConstructor) {
					var op = event.toConstructor [
						documentation = '''
							Construct an event. The source of the event is unknown.
						'''
						body = '''
							super();
						'''
					]
					op.annotations += toAnnotation(Generated)	
					members += op
					op = event.toConstructor [
						documentation = '''
							Construct an event.
							@param source - address of the agent that is emitting this event.
						'''
						parameters += event.toParameter('source', newTypeRef(Address))
						body = '''
							super(source);
						'''
					]
					op.annotations += toAnnotation(Generated)	
					members += op
				}
								
				if (!jvmFields.isEmpty) {

					val JvmField[] tab = jvmFields // single translation to the array
 					var elementType = event.toClass(event.fullyQualifiedName)
 					
					var op = toEqualsBugFix.toEqualsMethod(it, event, elementType, true, tab)
 					if (op!==null) {
						op.annotations += toAnnotation(Generated)	
						members += op
					}
					
					op = hashCodeBugFix.toHashCodeMethod(it, event, true, tab)
					if (op!==null) {
						op.annotations += toAnnotation(Generated)	
						members += op
					}
					
					op = event.toMethod("attributesToString", newTypeRef(String))[
						visibility = JvmVisibility::PROTECTED
						documentation = '''Returns a String representation of the Event «event.name» attributes only.'''
						body = [
							append(
								'''
								StringBuilder result = new StringBuilder(super.attributesToString());
								«FOR attr : event.features.filter(Attribute)»
									result.append("«attr.name»  = ").append(this.«attr.name»);
								«ENDFOR»
								return result.toString();''')
						]
					]
					if (op!==null) {
						op.annotations += toAnnotation(Generated)	
						members += op
					}
				}

				val serialValue = serial
				val serialField = event.toField("serialVersionUID", newTypeRef(long)) [
					visibility = JvmVisibility::PRIVATE
					final = true
					static = true
					initializer = [append(serialValue+"L")]
				]
				serialField.annotations += toAnnotation(Generated)	
				members += serialField
				readAndWriteTracking.markInitialized(serialField)

			])
	}

	def dispatch void infer(Capacity capacity, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(capacity.toInterface(capacity.fullyQualifiedName.toString, null)).initializeLater(
			[
				// Reset the action registry
				sarlSignatureProvider.resetSignatures(it)

				capacity.copyDocumentationTo(it)
				generateExtendedTypes(it, capacity, io.sarl.lang.core.Capacity)
				
				var actionIndex = 0
				for (feature : capacity.features) {
					if (feature!==null) {
						if (generateAction(feature as ActionSignature, null, actionIndex) !== null) {
							actionIndex++
						}
					}
				}
			])
	}

	def dispatch void infer(Skill skill, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(skill.toClass(skill.fullyQualifiedName)).initializeLater(
			[
				// Reset the action registry
				sarlSignatureProvider.resetSignatures(it)

				skill.copyDocumentationTo(it)

				generateExtendedTypes(it, skill, io.sarl.lang.core.Skill)
				generateImplementedTypes(it, skill, io.sarl.lang.core.Capacity)

				val finalOperations = newTreeMap(null)
				val overridableOperations = newTreeMap(null)
				val operationsToImplement = newTreeMap(null)
				populateInheritanceContext(
						it,
						finalOperations, overridableOperations,
						null, operationsToImplement,
						null, this.sarlSignatureProvider)
				
				var actionIndex = 0
				var hasConstructor = false
				
				for (feature : skill.features) {
					if (feature!==null) {
						switch feature {
							Action: {
								var sig = feature.signature as ActionSignature
								if (it.generateAction(
									sig,
									feature.body,
									actionIndex, false,
									operationsToImplement,
									overridableOperations
								) [ return !finalOperations.containsKey(it)
											&& !overridableOperations.containsKey(it)
								] !== null) {
									actionIndex++
								}
							}
							Constructor: {
								if (it.generateConstructor(skill, feature, actionIndex) !== null) {
									actionIndex++
									hasConstructor = true
								}
							}
							Attribute: {
								it.generateAttribute(feature, JvmVisibility::PROTECTED)
							}
							CapacityUses: {
								for (used : feature.capacitiesUsed) {
									actionIndex = generateCapacityDelegatorMethods(skill, used, actionIndex, operationsToImplement, overridableOperations)
								}
							}
						}
					}
				}
				
				actionIndex = generateMissedFunction(skill, actionIndex, operationsToImplement, overridableOperations)
								
				if (!hasConstructor) {
					it.members += skill.toConstructor [
						documentation = '''
							Construct a skill.
							@param owner - agent that is owning this skill. 
						'''
						parameters += skill.toParameter('owner', newTypeRef(io.sarl.lang.core.Agent))
						body = '''
							super(owner);
						'''
					]
					it.members += skill.toConstructor [
						documentation = '''
							Construct a skill. The owning agent is unknown. 
						'''
						body = '''
							super();
						'''
					]
				}
			])
	}

	def dispatch void infer(Behavior behavior, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(behavior.toClass(behavior.fullyQualifiedName)).initializeLater(
			[
				// Reset the action registry
				sarlSignatureProvider.resetSignatures(it)

				behavior.copyDocumentationTo(it)
				generateExtendedTypes(it, behavior, io.sarl.lang.core.Behavior)
				
				var behaviorUnitIndex = 1
				var actionIndex = 1
				var hasConstructor = false
				
				for (feature : behavior.features) {
					if (feature!==null) {
						switch feature {
							RequiredCapacity: {
								//TODO 
							}
							BehaviorUnit: {
								val bMethod = generateBehaviorUnit(feature, behaviorUnitIndex)
								if (bMethod !== null) {
									behaviorUnitIndex++						
									members += bMethod
								}
							}
							Action: {
								if (generateAction(feature.signature as ActionSignature, feature.body, actionIndex) !== null) {
									actionIndex++
								}
							}
							CapacityUses: {
								for (used : feature.capacitiesUsed) {
									actionIndex = generateCapacityDelegatorMethods(behavior, used, actionIndex, null, null)
								}
							}
							Constructor: {
								if (generateConstructor(behavior, feature, actionIndex) !== null) {
									actionIndex++
									hasConstructor = true
								}
							}
							Attribute: {
								generateAttribute(feature, JvmVisibility::PROTECTED)
							}
						}
					}
				}
				
				if (!hasConstructor) {
					members += behavior.toConstructor [
						documentation = '''
							Construct a behavior.
							@param owner - reference to the agent that is owning this behavior.
						'''
						parameters += behavior.toParameter('owner', newTypeRef(io.sarl.lang.core.Agent))
						body = '''
							super(owner);
						'''
					]
				}
			])
	}

	def dispatch void infer(Agent agent, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(agent.toClass(agent.fullyQualifiedName)).initializeLater [
			// Reset the action registry
			sarlSignatureProvider.resetSignatures(it)
			
			agent.copyDocumentationTo(it)
			generateExtendedTypes(agent, io.sarl.lang.core.Agent)
			var cons = agent.toConstructor [
				documentation = '''
					Construct an agent.
					@param parentID - identifier of the parent. It is the identifer
					of the parent agent and the enclosing contect, at the same time.
				'''
				parameters += agent.toParameter('parentID', newTypeRef(UUID))
				body = '''
					super(parentID);
				'''
			]
			cons.annotations += agent.toAnnotation(Generated)
			members += cons
			
			var behaviorUnitIndex = 1
			var actionIndex = 1
			
			for (feature : agent.features) {
				if (feature!==null) {
					switch feature {
						BehaviorUnit: {
							val bMethod = generateBehaviorUnit(feature, behaviorUnitIndex)
							if (bMethod !== null) {
								behaviorUnitIndex++
								members += bMethod
							}
						}
						Action: {
							if (generateAction(feature.signature as ActionSignature, feature.body, actionIndex) !== null) {
								actionIndex++
							}
						}
						Attribute: {
							generateAttribute(feature, JvmVisibility::PROTECTED)
						}
						CapacityUses: {
							for (used : feature.capacitiesUsed) {
								actionIndex = generateCapacityDelegatorMethods(agent, used, actionIndex, null, null)
							}
						}
					}
				}
			}
		]
	}
	
	protected def int generateMissedFunction(
		JvmGenericType output,
		EObject owner,
		int actionIndex,
		Map<ActionKey,JvmOperation> operationsToImplement,
		Map<ActionKey,JvmOperation> overridableOperations) {
			
		var actIndex = actionIndex
		var String currentKeyStr = null
		var JvmOperation originalOperation = null
		var SignatureKey sigKey = null
		
		for(missedOperation : operationsToImplement.entrySet) {
			var originalSignature = annotationString(missedOperation.value, DefaultValueUse)
			if (originalSignature!==null) {
				if (originalSignature!=currentKeyStr) {
					currentKeyStr = originalSignature
					sigKey = this.sarlSignatureProvider.createSignatureIDFromString(originalSignature)
					var key = this.sarlSignatureProvider.createActionID(missedOperation.key.functionName, sigKey)
					originalOperation = overridableOperations.get(key);
				}
				if (originalOperation!==null) {
					var op = owner.toMethod(originalOperation.simpleName, originalOperation.returnType, null)
					op.varArgs = originalOperation.varArgs
					op.final = true
					var args = newArrayList
					
					var it1 = missedOperation.value.parameters.iterator
					var it2 = originalOperation.parameters.iterator
					var JvmFormalParameter oparam = null
					
					while (it2.hasNext) {
						var param = it2.next
						var vId = annotationString(param, DefaultValue)
						if (oparam==null && it1.hasNext) {
							oparam = it1.next
						}
						if (oparam!==null && oparam.simpleName==param.simpleName) {
							args += oparam.simpleName
							op.parameters += param.toParameter(oparam.simpleName, oparam.parameterType)
							oparam = null
						}
						else if (vId!==null && !vId.empty) {
							args.add(
								originalOperation.declaringType.qualifiedName
								+".___FORMAL_PARAMETER_DEFAULT_VALUE_"
								+vId)
						}
						else {
							throw new IllegalStateException("Invalid generation of the default-valued formal parameters")
						}
					}
					
					{
						val tmpName = originalOperation.simpleName
						val tmpArgs = args
						op.body = [
							append(tmpName)
							append("(")
							append(IterableExtensions.join(tmpArgs, ", "))
							append(");")
						] //(new CallingFunctionGenerator(originalOperation.simpleName, args))
					}
					op.annotations += owner.toAnnotation(DefaultValueUse, originalSignature)
					output.members += op				
					actIndex++
				}
			}
		}
		return actIndex
	}

// FIXME: Null
	protected def long generateExtendedTypes(JvmGenericType owner, InheritingElement element, Class<?> defaultType) {
		var serial = 0L
		var isInterface = owner.interface
		for(JvmParameterizedTypeReference superType : element.superTypes) {
			if (superType.type instanceof JvmGenericType) {	
				var reference = toLightweightTypeReference(superType, services)
				if (reference.interfaceType===isInterface && reference.isSubtypeOf(defaultType)) {
					owner.superTypes += cloneWithProxies(superType)
					serial = serial + superType.identifier.hashCode
				}
			}
		}
		if (owner.superTypes.empty) {
			var type = element.newTypeRef(defaultType)
			owner.superTypes += type
			serial = serial + type.identifier.hashCode
		}
		return serial
	}
	
	protected def long generateImplementedTypes(JvmGenericType owner, ImplementingElement element, Class<?> mandatoryType) {
		var serial = 0L
		for(JvmParameterizedTypeReference implementedType : element.implementedTypes) {
			if (implementedType.type instanceof JvmGenericType) {	
				var reference = toLightweightTypeReference(implementedType, services)
				if (reference.interfaceType && reference.isSubtypeOf(mandatoryType)) {
					owner.superTypes += cloneWithProxies(implementedType)
					serial = serial + implementedType.identifier.hashCode
				}
			}
		}
		return serial
	}

	protected def JvmField generateAttribute(JvmGenericType owner, Attribute attr, JvmVisibility attrVisibility) {
		var field = attr.toField(attr.name, attr.type) [
			visibility = attrVisibility
			attr.copyDocumentationTo(it)
			final = (!attr.writeable)
			static = (!attr.writeable) && (attr.initialValue!==null)
			initializer = attr.initialValue
		]
		owner.members += field
		if (attr.initialValue!==null) {
			readAndWriteTracking.markInitialized(field)
		}
		return field
	}
	
	protected def int generateCapacityDelegatorMethods(
		JvmGenericType owner, InheritingElement context,
		JvmParameterizedTypeReference capacityType, int index,
		Map<ActionKey,JvmOperation> operationsToImplement,
		Map<ActionKey,JvmOperation> implementedOperations) {
		
		if (capacityType.type instanceof JvmGenericType) {	
			var reference = toLightweightTypeReference(capacityType, services)
			if (reference.isSubtypeOf(io.sarl.lang.core.Capacity)) {
				var actionIndex = index
				val capacityOperations = newTreeMap(null)
				
				populateInterfaceElements(
						capacityType.type as JvmGenericType,
						capacityOperations,
						null, this.sarlSignatureProvider)
		
				for(entry : capacityOperations.entrySet) {
					if (implementedOperations===null || !implementedOperations.containsKey(entry.key)) {
						var op = context.toMethod(entry.value.simpleName, entry.value.returnType) [
							visibility = JvmVisibility::PROTECTED
							val args = newArrayList
							for(param : entry.value.parameters) {
								parameters += context.toParameter(param.simpleName, param.parameterType)
								args += param.simpleName
							}
							body = [
								if (entry.value.returnType.identifier!='void') {
									append("return ")
								}
								append("getSkill(")
								append(entry.value.declaringType.qualifiedName)
								append(".class).")
								append(entry.value.simpleName)
								append("(")
								append(args.join(", "))
								append(");")
							]
						]
						op.annotations += context.toAnnotation(Generated)
						owner.members += op
						// 
						if (operationsToImplement!==null) operationsToImplement.remove(entry.key)
						if (implementedOperations!==null) implementedOperations.put(entry.key, entry.value)
						actionIndex++
					}
				}
				return actionIndex
			}
		}
		return index
	}

	protected def JvmOperation generateBehaviorUnit(JvmGenericType owner, BehaviorUnit unit, int index) {
		if (unit.event!==null) {
			var isTrueGuard = false
			val guard = unit.guard
			
			if (guard == null) {
				isTrueGuard = true
			}
			else if (guard instanceof XBooleanLiteral) {
				if (guard.isTrue) {
					isTrueGuard = true
				}
				else {
					// The guard is always false => no need to generate the code
					return null
				}
			}

			val voidType = unit.newTypeRef(Void::TYPE)
			val behName = "_handle_" + unit.event.simpleName + "_" + index
			
			val behaviorMethod = unit.toMethod(behName, voidType) [
				unit.copyDocumentationTo(it)
				annotations += unit.toAnnotation(Percept)
				parameters +=
					unit.event.toParameter(SARLKeywords::OCCURRENCE, unit.event)
			]
						
			if (isTrueGuard) {
				behaviorMethod.body = unit.body
			} else {
				val guardMethodName = behName + "_Guard"
				val guardMethod = guard.toMethod(guardMethodName, guard.newTypeRef(Boolean::TYPE)) [
					documentation = "Ensures that the behavior " + behName + " is called only when the guard " +
						guard.toString + " is valid"
					parameters += unit.event.toParameter(SARLKeywords::OCCURRENCE, unit.event)
					body = guard
				]
	
				jvmModelAssociator.associateLogicalContainer(unit.body, behaviorMethod)
	
				behaviorMethod.body = [
					it.append('''if ( «guardMethodName»(«SARLKeywords::OCCURRENCE»)) { ''')
					xbaseCompiler.compile(unit.body, it, voidType, null)
					it.append('}')
				]
	
				owner.members += guardMethod
			}
			return behaviorMethod
		}
		log.fine("Unable to resolve the event for a behavior unit")
		return null
	}
	
	protected def List<String> generateFormalParametersAndDefaultValueFields(
		JvmExecutable owner, JvmGenericType actionContainer, 
		EObject sourceElement, boolean varargs,
		List<FormalParameter> params, 
		boolean isForInterface,
		int actionIndex) {

		var parameterTypes = newArrayList
		var JvmFormalParameter lastParam = null
		var paramIndex = 0
		var hasDefaultValue = false
		for (param : params) {
			val paramName = param.name
			val paramType = param.parameterType
			
			if (paramName!==null && paramType!==null) {
				lastParam = param.toParameter(paramName, paramType)
	
				if (param.defaultValue!==null) {
					hasDefaultValue = true
					var namePostPart = actionIndex+"_"+paramIndex
					var name = "___FORMAL_PARAMETER_DEFAULT_VALUE_"+namePostPart
					// FIXME: Hide these attributes into an inner interface.
					var field = param.defaultValue.toField(name, paramType) [
						documentation = "Default value for the parameter "+paramName
						static = true
						final = true
						if (isForInterface) {
							visibility = JvmVisibility::PUBLIC
						}
						else {
							visibility = JvmVisibility::PRIVATE
						}
						initializer = param.defaultValue
					]
					field.annotations += param.toAnnotation(Generated)
					actionContainer.members += field
					readAndWriteTracking.markInitialized(field)
					var annot = param.toAnnotation(DefaultValue, namePostPart)
					lastParam.annotations += annot
				}
				
				owner.parameters += lastParam
				parameterTypes.add(paramType.identifier)
				
				paramIndex++
			}
		}
		if (varargs && lastParam !== null) {
			lastParam.parameterType = lastParam.parameterType.addArrayTypeDimension
		}
		if (hasDefaultValue) {
			owner.annotations += sourceElement.toAnnotation(DefaultValueSource)
		}
		return parameterTypes
	}

	protected def List<String> generateFormalParametersWithDefaultValue(JvmExecutable owner, JvmGenericType actionContainer, boolean varargs, List<InferredStandardParameter> signature, int actionIndex) {
		var JvmFormalParameter lastParam = null
		val arguments = newArrayList
		var paramIndex = 0
		for(parameterSpec : signature) {
			if (parameterSpec instanceof InferredValuedParameter) {
				arguments.add("___FORMAL_PARAMETER_DEFAULT_VALUE_"+actionIndex+"_"+paramIndex)
			}
			else {
				val param = parameterSpec.parameter
				val paramName = param.name
				val paramType = param.parameterType
				if (paramName!==null && paramType!==null) {
					lastParam = param.toParameter(paramName, paramType)
					owner.parameters += lastParam
					arguments.add(paramName)
				}
			}
			paramIndex++
		}
		if (varargs && lastParam !== null) {
			lastParam.parameterType = lastParam.parameterType.addArrayTypeDimension
		}
		return arguments
	}

	protected final def JvmOperation generateAction(
		JvmGenericType owner, ActionSignature signature, 
		XExpression operationBody, int index) {
		return generateAction(owner, signature, operationBody,
			index, operationBody===null, null,
			null, null
		)		
	}

	protected def JvmOperation generateAction(
		JvmGenericType owner, ActionSignature signature,
		XExpression operationBody, int index, boolean isAbstract,
		Map<ActionKey,JvmOperation> operationsToImplement,
		Map<ActionKey,JvmOperation> implementedOperations,
		(ActionKey) => boolean inheritedOperation) {
			
		var returnType = signature.type
		if (returnType == null) {
			returnType = signature.newTypeRef(Void::TYPE)
		}
		
		val actionKey = sarlSignatureProvider.createFunctionID(owner, signature.name)
				
		var mainOp = signature.toMethod(signature.name, returnType) [
			signature.copyDocumentationTo(it)
			varArgs = signature.varargs
			abstract = isAbstract
			generateFormalParametersAndDefaultValueFields(
				owner, signature, signature.varargs, signature.params, isAbstract, index
			)
			body = operationBody
		]
		owner.members += mainOp
		
		val otherSignatures = sarlSignatureProvider.createSignature(
			actionKey,
			signature.varargs, signature.params
		)

		var actSigKey = sarlSignatureProvider.createActionID(
					signature.name,
					otherSignatures.formalParameterKey
				)
		if (operationsToImplement!==null) {
			var removedOp = operationsToImplement.remove(actSigKey)
			if (removedOp!==null && implementedOperations!==null) {
				implementedOperations.put(actSigKey, removedOp)
			}
		}
		
		for(otherSignature : otherSignatures.getInferredSignatures().entrySet) {
			var ak = sarlSignatureProvider.createActionID(
					signature.name,
					otherSignature.key
				)
			if (inheritedOperation==null || 
				inheritedOperation.apply(ak)) {
				var additionalOp = signature.toMethod(signature.name, returnType) [
					signature.copyDocumentationTo(it)
					varArgs = signature.varargs
					final = !isAbstract
					abstract = isAbstract
					val args = generateFormalParametersWithDefaultValue(
						owner, signature.varargs, otherSignature.value, index
					)
					if (!isAbstract) {
						body = [
							append(signature.name)
							append("(")
							append(args.join(", "))
							append(");")
						]
					}
					annotations += signature.toAnnotation(
						DefaultValueUse, 
						otherSignatures.formalParameterKey.toString
					)
				]
				owner.members += additionalOp
	
				if (operationsToImplement!==null) {
					var removedOp = operationsToImplement.remove(ak)
					if (removedOp!==null && implementedOperations!==null) {
						implementedOperations.put(ak, removedOp)
					}
				}
			}
		}

		return mainOp
	}

	protected def SignatureKey generateConstructor(JvmGenericType owner, TopElement context, Constructor constructor, int index) {
		val actionKey = sarlSignatureProvider.createConstructorID(owner)
		
		owner.members += constructor.toConstructor [
			constructor.copyDocumentationTo(it)
			varArgs = constructor.varargs
			generateFormalParametersAndDefaultValueFields(
				owner, constructor, constructor.varargs, constructor.params, false, index
			)
			body = constructor.body
		]

		val otherSignatures = sarlSignatureProvider.createSignature(
			actionKey,
			constructor.varargs, constructor.params
		)
		
		for(otherSignature : otherSignatures) {
			var op = constructor.toConstructor [
				constructor.copyDocumentationTo(it)
				varArgs = constructor.varargs
				val args = generateFormalParametersWithDefaultValue(
					owner, constructor.varargs, otherSignature, index
				)
				body = [
					append("this(")
					append(args.join(", "))
					append(");")
				]
				annotations += constructor.toAnnotation(
					DefaultValueUse,
					otherSignatures.formalParameterKey.toString
				)
			]
			owner.members += op
		}
		
		return otherSignatures.formalParameterKey
	}
			
}
