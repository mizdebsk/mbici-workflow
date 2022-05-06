/*-
 * Copyright (c) 2022 Red Hat, Inc.
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
package org.fedoraproject.mbi.ci.report;

import org.fedoraproject.mbi.ci.model.Plan;

/**
 * @author Mikolaj Izdebski
 */
public class PlanReport
    extends Report
{
    private final Plan plan;

    public PlanReport( Plan plan )
    {
        this.plan = plan;
    }

    @Override
    public void body()
    {
        header( "Test plan" );
        para( "Plan describes a way to build RPM packages. It specifies what ",
              "packages are built, in which order and with what RPM macros defined." );
        para( "Plan consists of consecutive phases. Each phase consists of a ",
              "number of independant RPM builds that can be done in parallel. ",
              "Once all RPMs in particular phase are successfully built, a YUM repository ",
              "is created out of them and made available to subsequent phases." );
        para( "Plan does not specify sources for each component. That information is part of ",
              link( "subject.html", "test subject" ), "." );
        para( "Machine-readable information about test plan in XML format can be found in ",
              link( "plan.xml", "plan.xml" ), ". Human-readable information is included below." );

        subtitle( "Global macros" );
        list( "RPM macros that are defined for all component builds in the whole plan:",
              "There are no global macros defined.", plan.getMacros(),
              macro -> macro.getName() + ": " + macro.getValue() );

        for ( var phase : plan.getPhases() )
        {
            subtitle( "Phase " + phase.getName() );
            list( "RPM macros specific to this phase:", "There are no specific macros defined for this phase.",
                  phase.getMacros(), macro -> macro.getName() + ": " + macro.getValue() );
            list( "Components built in this phase:", "There are no components in this phase.", phase.getComponents(),
                  component -> component );
        }

        footer();
    }
}
