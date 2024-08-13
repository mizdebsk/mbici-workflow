/*-
 * Copyright (c) 2021 Red Hat, Inc.
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
package org.fedoraproject.mbi.ci.generate;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.fedoraproject.mbi.ci.Command;
import org.fedoraproject.mbi.ci.model.Plan;
import org.fedoraproject.mbi.ci.model.Platform;
import org.fedoraproject.mbi.ci.model.Subject;
import org.fedoraproject.mbi.wf.model.Workflow;
import org.fedoraproject.mbi.xml.Builder;
import org.fedoraproject.mbi.xml.Entity;

/**
 * @author Mikolaj Izdebski
 */
public class GenerateCommand
    extends Command
{
    private final Path planPath;

    private final Path platformPath;

    private final Path subjectPath;

    private final Path workflowPath;

    private final boolean validate;

    public GenerateCommand( Path planPath, Path platformPath, Path subjectPath, Path workflowPath, boolean validate )
    {
        this.planPath = planPath;
        this.platformPath = platformPath;
        this.subjectPath = subjectPath;
        this.workflowPath = workflowPath;
        this.validate = validate;
    }

    @Override
    public void run()
        throws Exception
    {
        Plan plan = Plan.readFromXML( planPath );
        Platform platform = Platform.readFromXML( platformPath );
        Subject subject = Subject.readFromXML( subjectPath );

        WorkflowFactory wff = new WorkflowFactory();
        Workflow wfd = wff.createFromPlan( platform, plan, subject, validate );
        wfd.writeToXML( workflowPath );
    }

    private static final class ArgsBuilder
        implements Builder<GenerateCommand>
    {
        private Path planPath;

        private Path platformPath;

        private Path subjectPath;

        private Path workflowPath;

        private boolean validate;

        public void setPlanPath( Path planPath )
        {
            this.planPath = planPath;
        }

        public void setPlatformPath( Path platformPath )
        {
            this.platformPath = platformPath;
        }

        public void setSubjectPath( Path subjectPath )
        {
            this.subjectPath = subjectPath;
        }

        public void setWorkflowPath( Path workflowPath )
        {
            this.workflowPath = workflowPath;
        }

        public void setValidate( String dummy )
        {
            validate = true;
        }

        @Override
        public GenerateCommand build()
        {
            return new GenerateCommand( planPath.toAbsolutePath(), platformPath.toAbsolutePath(),
                                        subjectPath.toAbsolutePath(), workflowPath.toAbsolutePath(), validate );
        }
    }

    public static final Entity<GenerateCommand, ArgsBuilder> ENTITY = new Entity<>( "generate", ArgsBuilder::new );
    static
    {
        ENTITY.addAttribute( "plan", x -> null, ArgsBuilder::setPlanPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "platform", x -> null, ArgsBuilder::setPlatformPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "subject", x -> null, ArgsBuilder::setSubjectPath, Path::toString, Paths::get );
        ENTITY.addAttribute( "workflow", x -> null, ArgsBuilder::setWorkflowPath, Path::toString, Paths::get );
        ENTITY.addOptionalAttribute( "validate", x -> null, ArgsBuilder::setValidate );
    }
}