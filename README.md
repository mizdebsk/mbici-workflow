MBICI Workflow
==============

MBICI implements Continuous Integration (CI) for Maven Bootstrap
Initiative (MBI).

MBICI Workflow is an RPM package build and test engine that is used
for continuously testing bootstrapping of Maven RPM packages.


Runtime dependencies
--------------------

The following external dependencies are required to run MBICI
Workflow:

* `git-core` - for cloning dist-git repositories
* `curl` - for downloading blobs from lookaside cache
* `dnf5` - for downloading platform RPMs
* `mock` - for running RPM builds
* `createrepo_c` - for creating YUM repositories out of built RPM
  packages

Optional dependencies:

* `kubectl` - for executing tasks on Kubernetes cluster


Quick Start
-----------

Compile the sources and assemble binary executable `mbi`:

    mvn clean verify

Generate Workflow from example Build Plan, Platform and Test Subject:

    ./target/mbi generate \
        --plan examples/dummy-plan.xml \
        --platform examples/dummy-platform.xml \
        --subject examples/dummy-subject.xml \
        --workflow /tmp/mbi.xml

Execute generated workflow:

    ./target/mbi run \
        --workflow /tmp/mbi.xml \
        --result-dir /tmp/mbici-result \
        --cache-dir /tmp/mbici-cache \
        --work-dir /tmp/mbici-work

Generate a HTML report:

    ./target/mbi report \
        --plan examples/dummy-plan.xml
        --platform examples/dummy-platform.xml \
        --subject examples/dummy-subject.xml \
        --workflow /tmp/mbi.xml \
        --result-dir /tmp/mbici-result \
        --report-dir /tmp/mbici-report


Build Plan
----------

Build Plan describes a way to build RPM packages.  It specifies what
components are built, in which order and with what RPM macros defined.

Build Plan consists of consecutive phases.  Each phase consists of a
number of independant RPM builds that can be done in parallel.  Once
all RPMs in particular phase are successfully built, a YUM repository
is created out of them and made available to subsequent phases.

Build Plan does not specify sources for each component.  That
information is part of Test Subject.

An example Build Plan that consists of two phases called `first` and
`second`:

    <plan>
      <macro>
        <name>global_macro</name>
        <value>this macro is defined for all builds</value>
      </macro>
      <phase>
        <name>first-phase</name>
        <macro>
          <name>phase_specific_macro</name>
          <value>this macro is defined builds of -gloster and -crested packages</value>
        </macro>
        <component>dummy-test-package-gloster</component>
        <component>dummy-test-package-crested</component>
      </phase>
      <phase>
        <name>second-phase</name>
        <component>dummy-test-package-rubino</component>
      </phase>
    </plan>


Test Subject
------------

Test Subject is a concrete set of dist-git repository commits which
are used as sources for building RPM packages.

For each component that is part of a Build Plan, Test Subject
specifies dist-git URL and exact commit hash, as well as URL of
lookaside cache used to download source blobs from.

An example Test Subject:

    <subject>
      <component>
        <name>dummy-test-package-crested</name>
        <scm>https://src.fedoraproject.org/rpms/dummy-test-package-crested.git</scm>
        <commit>f3120164f809b78afbf6e39e50b33e7ad0569858</commit>
        <lookaside>https://src.fedoraproject.org/lookaside/pkgs/rpms/dummy-test-package-crested</lookaside>
      </component>
      <component>
        <name>dummy-test-package-gloster</name>
        <scm>https://src.fedoraproject.org/rpms/dummy-test-package-gloster.git</scm>
        <commit>debb3f32a12125f5bfcb0e431193a0b7339ed6ff</commit>
        <lookaside>https://src.fedoraproject.org/lookaside/pkgs/rpms/dummy-test-package-gloster</lookaside>
      </component>
      <component>
        <name>dummy-test-package-rubino</name>
        <scm>https://src.fedoraproject.org/rpms/dummy-test-package-rubino.git</scm>
        <commit>699a8aa59667d44f3073717114cac6a300132e7c</commit>
        <lookaside>https://src.fedoraproject.org/lookaside/pkgs/rpms/dummy-test-package-rubino</lookaside>
      </component>
    </subject>


Platform
--------

Platform is a subset of an RPM-based operating system that contains
packages sufficient to build all RPM packages listed in Test Subject
according to Build Plan.

Platform is defined by one or more YUM repositories with a number of
RPM packages that are taken from these repositories, along with their
runtime dependencies.

Platform is typically only a small subset of the whole OS for two reasons: to
improve build performance and to prevent new dependencies on OS
packages from getting in unnoticed.

An example Platform:

    <platform>
      <repo>
        <name>Everything</name>
        <url>https://ftp.icm.edu.pl/pub/Linux/dist/fedora/linux/development/rawhide/Everything/x86_64/os/</url>
      </repo>
      <package>rpm-build</package>
      <package>glibc-minimal-langpack</package>
    </platform>


Workflow
--------

Workflow is a set of tasks, which all must be successfully completed
in order for the workflow to be done.

Tasks that constitute workflow are steps necessary to build RPM
packages from sources specified by Test Subject on given Platform, in
the way defined by Build Plan.

Workflow can be generated from Build Plan, Platform and Test Subject
by `mbi generate` command.


Usage
-----

* `mbi generate` - generate Workflow from given Build Plan,
  Platform and Test Subject

  Parameters:

  * `--plan <path>` - path to a Build Plan in XML format

  * `--platform <path>` - path to a Platform in XML format

  * `--subject <path>` - path to a Test Subject in XML format

  * `--workflow <path>` - path where generated Workflow should be
    written

* `mbi run` - execute Workflow and update it in-place

  Parameters:

  * `--workflow <path>` - path to Workflow

  * `--result-dir <path>` - path to a directory where task results and
    artifacts are written

  * `--cache-dir <path>` - path to a directory where dist-git commits
    and lookaside blobs are cached

  * `--work-dir <path>` - path to a directory under which temporary
    working directories for tasks are created

  * `--kubernetes-ns` - build SRPM and RPM packages on external Kubernetes
    cluster instead of local machine (requires `kubectl`)

* `mbi report` - generate a simple HTML report describing given
  Workflow

  Parameters:

  * `--plan <path>` - path to a Build Plan in XML format

  * `--platform <path>` - path to a Platform in XML format

  * `--subject <path>` - path to a Test Subject in XML format

  * `--workflow <path>` - path to Workflow

  * `--result-dir <path>` - path to a directory where task results are
    stored

  * `--report-dir <path>` - path to a directory where the report is
    written


Copying
-------

MBICI Workflow is free software.  You can redistribute and/or modify
it under the terms specified in the LICENSE file.  This software comes
with ABSOLUTELY NO WARRANTY.
