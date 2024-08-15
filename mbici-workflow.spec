%global debug_package %{nil}

Name:           mbici-workflow
Version:        1.0.0~SNAPSHOT
Release:        2%{?dist}
Summary:        MBICI Workflow
License:        Apache-2.0
URL:            https://github.com/mizdebsk/mbici-workflow

# git archive HEAD --prefix mbici-workflow-1.0.0~SNAPSHOT/ | gzip -9nc >mbici-workflow.tar.gz
Source0:        mbici-workflow.tar.gz

BuildRequires:  java-22-mandrel-devel
BuildRequires:  maven
BuildRequires:  rubygem-asciidoctor

Requires:       git-core
Requires:       curl
Requires:       dnf5
Requires:       mock
Requires:       createrepo_c

%description
MBICI implements Continuous Integration (CI) for Maven Bootstrap
Initiative (MBI).

MBICI Workflow is an RPM package build and test engine that is used
for continuously testing bootstrapping of Maven RPM packages.

%prep
%setup -q

%build
export JAVA_HOME=%{_jvmdir}/java-22-mandrel
export PATH=${JAVA_HOME}/bin:${PATH}

# Bulid Java JAR
mvn clean verify

# Generate native binary
native-image -jar target/mbici-workflow.jar

# Generate manpages
java -cp $(echo -n $HOME/.m2/repository/info/picocli/picocli-codegen/*/picocli-codegen-*.jar):$(echo -n $HOME/.m2/repository/info/picocli/picocli/*/picocli-*.jar):target/classes picocli.codegen.docgen.manpage.ManPageGenerator -d man io.kojan.mbici.Main
asciidoctor -b manpage man/*.adoc

# Generate bash completion
java -cp $(echo -n $HOME/.m2/repository/info/picocli/picocli/*/picocli-*.jar):target/classes picocli.AutoComplete io.kojan.mbici.Main

%install
# Install native binary
install -d -m 755 %{buildroot}%{_bindir}
install -p -m 755 mbici-workflow %{buildroot}%{_bindir}/mbici-wf

# Install manpages
install -d -m 755 %{buildroot}%{_mandir}/man1/
cp -a man/*.1 %{buildroot}%{_mandir}/man1/

# Install bash completion
install -d -m 755 %{buildroot}%{_datadir}/bash-completion/completions/
install -p -m 644 mbici-wf_completion %{buildroot}%{_datadir}/bash-completion/completions/mbici-wf


%files
%{_bindir}/mbici-wf
%{_mandir}/man1/*
%{_datadir}/bash-completion/completions/*
%license LICENSE AUTHORS

%changelog
* Thu Aug 15 2024 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.0.0~SNAPSHOT-2
- Update to latest snapshot

* Tue Aug 13 2024 Mikolaj Izdebski <mizdebsk@redhat.com> - 1.0.0~SNAPSHOT-1
- Initial packaging
