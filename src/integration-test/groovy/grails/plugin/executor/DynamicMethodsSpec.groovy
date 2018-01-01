/*
 * Copyright 2011 the original author or authors.
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

package grails.plugin.executor

import grails.test.mixin.integration.Integration

import grails.util.Holders
import grails.plugins.GrailsPlugin

import org.grails.core.artefact.*

import executor.test.Book

import spock.lang.Specification
import spock.lang.Unroll

@Integration
class DynamicMethodsSpec extends Specification {

	// Autowired
	def grailsApplication

	def currentClazz

	@Unroll("Reloading support for #clazz")
	def reloading() {
		setup:
		currentClazz = clazz

		expect:
		artifactHasExecutorMethods
/*
		when:
		reloadClass()

		then:
		!artifactHasExecutorMethods
*/
		when:
		informOfClassChange()

		then:
		artifactHasExecutorMethods

		where:
		clazz << [TestService, TestController]
	}

	def "domain classes"() {
		setup:
		currentClazz = Book

		expect:
		grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, currentClazz)

		when:
		informOfClassChange()

		then:
		artifactHasExecutorMethods
	}

	protected getArtifactHasExecutorMethods() {
		createArtifact().respondsTo("runAsync", Runnable).size() > 0
	}

	protected createArtifact() {
		loadClass().newInstance()
	}

	protected loadClass() {
		grailsApplication.classLoader.loadClass(currentClazz.name)
	}

	protected reloadClass() {
		grailsApplication.classLoader.clearCache()
		currentClazz = loadClass()
	}

	protected informOfClassChange() {
		Holders.currentPluginManager().getGrailsPlugin("executor").notifyOfEvent(
			GrailsPlugin.EVENT_ON_CHANGE, 
			currentClazz
		)
	}
}
