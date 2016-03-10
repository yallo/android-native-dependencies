/*
 * Copyright (C) 2014 Nabil HACHICHA.
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

package com.nabilhachicha.nativedependencies

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskInstantiationException
import org.gradle.api.Plugin
import com.nabilhachicha.nativedependencies.extension.NativeDependenciesExtension
import com.nabilhachicha.nativedependencies.task.NativeDependenciesResolverTask

class NativeDependenciesPlugin implements Plugin<Project> {
    final static PLUGIN_NAME = "native_dependencies"
    final static TASK_NAME = "resolveNativeDependencies"
    final static TASK_GROUP = "Android"
    final static TASK_DESCRIPTION = "Resolve native dependencies (.so)"
    final static TASK_ATTACH_TO_LIFECYCLE = "preBuild"

    def void apply(Project project) {
        verifyRequiredPlugins project

        project.configure(project) {
            extensions.create(PLUGIN_NAME, NativeDependenciesExtension)
        }

        project.afterEvaluate { evaluateResult ->
            if (null == evaluateResult.state.getFailure()) {
                Task task = project.task(TASK_NAME, type: NativeDependenciesResolverTask)
                task.setDescription(TASK_DESCRIPTION)
                task.setGroup(TASK_GROUP)
                task.
                	dependencies = project.native_dependencies.dependencies

                project.tasks.findByName(TASK_ATTACH_TO_LIFECYCLE).dependsOn task
            }
        }
    }

    private static void verifyRequiredPlugins(Project project) {
        Class<?> appPlugin = loadClass("com.android.build.gradle.AppPlugin");
        if (appPlugin != null && project.plugins.hasPlugin(appPlugin)) return;
        Class<?> libraryPlugin = loadClass("com.android.build.gradle.LibraryPlugin");
        if (libraryPlugin != null && project.plugins.hasPlugin(libraryPlugin)) return;
        Class<?> experimentalAppPlugin = loadClass("com.android.build.gradle.model.AppComponentModelPlugin");
        if (experimentalAppPlugin != null && project.plugins.hasPlugin(experimentalAppPlugin)) return;
        Class<?> experimentalLibraryPlugin = loadClass("com.android.build.gradle.model.LibraryComponentModelPlugin");
        if (experimentalLibraryPlugin != null && project.plugins.hasPlugin(experimentalLibraryPlugin)) return;


        throw new TaskInstantiationException("'android' or 'android-library' plugin has to be applied before")
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch(all) {
            return null;
        }
    }
}
