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
package org.fedoraproject.mbi.wf.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.fedoraproject.mbi.wf.ArtifactManager;
import org.fedoraproject.mbi.wf.TaskExecution;
import org.fedoraproject.mbi.wf.TaskTermination;
import org.fedoraproject.mbi.wf.model.ArtifactType;

/**
 * @author Mikolaj Izdebski
 */
class Mock
{
    private Map<String, String> macros = new LinkedHashMap<>();

    public void run( TaskExecution task, String... args )
        throws TaskTermination
    {
        ArtifactManager am = task.getArtifactManager();

        Path mockConfPath = am.create( ArtifactType.CONFIG, "mock.cfg" );
        try ( BufferedWriter bw = Files.newBufferedWriter( mockConfPath ) )
        {
            bw.write( "config_opts['basedir'] = '" + task.getWorkDir() + "'\n" );
            bw.write( "config_opts['cache_topdir'] = '" + task.getWorkDir() + "'\n" );
            bw.write( "" );
            bw.write( "config_opts['rpmbuild_networking'] = False\n" );
            bw.write( "config_opts['use_host_resolv'] = False\n" );
            bw.write( "config_opts['isolation'] = 'chroot'\n" );
            bw.write( "config_opts['use_bootstrap'] = False\n" );
            bw.write( "config_opts['plugin_conf']['yum_cache_enable'] = False\n" );
            bw.write( "config_opts['plugin_conf']['root_cache_enable'] = False\n" );
            bw.write( "config_opts['plugin_conf']['package_state_enable'] = False\n" );
            bw.write( "config_opts['plugin_conf']['tmpfs_enable'] = True\n" );
            bw.write( "config_opts['plugin_conf']['tmpfs_opts']['required_ram_mb'] = 2048\n" );
            bw.write( "config_opts['plugin_conf']['tmpfs_opts']['max_fs_size'] = '4g'\n" );
            bw.write( "config_opts['nosync'] = True\n" );
            bw.write( "config_opts['nosync_force'] = True\n" );
            bw.write( "config_opts['root'] = 'mock-chroot'\n" );
            bw.write( "config_opts['target_arch'] = 'x86_64'\n" );
            bw.write( "config_opts['chroot_setup_cmd'] = 'install shadow-utils rpm-build'\n" );
            bw.write( "\n" );
            for ( var macro : macros.entrySet() )
            {
                bw.write( "config_opts['macros']['%" + macro.getKey() + "'] = '" + macro.getValue() + "'\n" );
            }
            bw.write( "\n" );
            bw.write( "config_opts['yum.conf'] = \"\"\"\n" );
            bw.write( "[main]\n" );
            bw.write( "keepcache=0\n" );
            bw.write( "debuglevel=1\n" );
            bw.write( "reposdir=/dev/null\n" );
            bw.write( "gpgcheck=0\n" );
            bw.write( "assumeyes=1\n" );
            bw.write( "install_weak_deps=0\n" );
            bw.write( "metadata_expire=-1\n" );

            int priority = 0;
            for ( Path repoPath : am.getDepArtifactsByType( ArtifactType.REPO, task ) )
            {
                String name = repoPath.getParent().getParent().getFileName().toString();
                bw.write( "\n" );
                bw.write( "[" + name + "]\n" );
                bw.write( "name=" + name + "\n" );
                bw.write( "baseurl=" + repoPath.getParent() + "\n" );
                bw.write( "priority=" + ++priority + "\n" );
            }

            bw.write( "\"\"\"\n" );
        }
        catch ( IOException e )
        {
            TaskTermination.error( "I/O error when writing mock config: " + e.getMessage() );
        }

        for ( String logName : Arrays.asList( "build.log", "root.log", "hw_info.log", "state.log" ) )
        {
            am.create( ArtifactType.LOG, logName );
        }

        Command cmd = new Command( task, 600, "mock" );
        cmd.addArg( "-r", mockConfPath.toString() );
        cmd.addArg( "--resultdir", task.getResultDir().toString() );
        cmd.addArg( args );
        cmd.run();
    }

    public void addMacro( String name, String value )
    {
        this.macros.put( name, value );
    }
}
