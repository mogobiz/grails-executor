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

import grails.plugin.executor.PersistenceContextExecutorWrapper
import org.grails.core.artefact.*

import java.util.concurrent.Executors

class ExecutorGrailsPlugin {

	def grailsVersion = "3.0 > *"

	def author = "Joshua Burnett"
	def authorEmail = "joshua@greenbill.com"
	def title = "Concurrency / asynchronous /background process plugin"
	def description = "its all concurrent baby."
	def documentation = "http://github.com/basejump/grails-executor"

	def license = 'APACHE'
	def issueManagement = [system: 'GITHUB', url: 'https://github.com/basejump/grails-executor/issues']
	def scm = [url: 'https://github.com/basejump/grails-executor']

	def observe = ["controllers","services","domain"]

	def pluginExcludes = [
		"grails-app/**",
		"src/main/webapp/**"
	]

	def doWithSpring = {
		executorService(PersistenceContextExecutorWrapper) { bean ->
			bean.destroyMethod = 'destroy'
			persistenceInterceptor = ref("persistenceInterceptor")
			executor = Executors.newCachedThreadPool()
		}
	}

	def addAsyncMethods(application, clazz) {
		clazz.metaClass.runAsync = { Runnable runme ->
			application.mainContext.executorService.withPersistence(runme)
		}
		clazz.metaClass.callAsync = { Closure clos ->
			application.mainContext.executorService.withPersistence(clos)
		}
		clazz.metaClass.callAsync = { Runnable runme, returnval ->
			application.mainContext.executorService.withPersistence(runme, returnval)
		}
	}

	def doWithDynamicMethods = { ctx ->
		for (artifactClasses in [
			application.getArtefacts(ControllerArtefactHandler.TYPE), 
			application.getArtefacts(ServiceArtefactHandler.TYPE), 
			application.getArtefacts(DomainClassArtefactHandler.TYPE)]) {
			for (clazz in artifactClasses) {
				addAsyncMethods(application, clazz)
			}
		}
	}

	def onChange = { event ->
		if (
			application.isArtefactOfType(ControllerArtefactHandler.TYPE, event.source) || 
			application.isArtefactOfType(ServiceArtefactHandler.TYPE, event.source) || 
			application.isArtefactOfType(DomainClassArtefactHandler.TYPE, event.source)) {
			addAsyncMethods(application, event.source)
		}
	}

}
