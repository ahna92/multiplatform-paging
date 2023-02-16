Pod::Spec.new do |spec|
    spec.name                     = 'shared_composeui'
    spec.version                  = '3.2.0-alpha03-0.2.0-SNAPSHOT'
    spec.homepage                 = 'https://github.com/cashapp/multiplatform-paging/tree/main/samples/repo-search/shared-composeui'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Shared Compose UI code for Repo Search.'
    spec.vendored_frameworks      = 'build/cocoapods/framework/shared_composeui.framework'
    spec.libraries                = 'c++'
                
                
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':samples:repo-search:shared-composeui',
        'PRODUCT_MODULE_NAME' => 'shared_composeui',
    }
                
    spec.script_phases = [
        {
            :name => 'Build shared_composeui',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$COCOAPODS_SKIP_KOTLIN_BUILD" ]; then
                  echo "Skipping Gradle build task invocation due to COCOAPODS_SKIP_KOTLIN_BUILD environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end