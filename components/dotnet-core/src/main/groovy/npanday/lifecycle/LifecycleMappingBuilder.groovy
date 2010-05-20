package npanday.lifecycle;

import npanday.ArtifactType 

class LifecycleMappingBuilder {
	
	LifecycleMapping mapping
	
	LifecycleMappingBuilder(ArtifactType type){
		mapping = new LifecycleMapping(type: type)

	}
	
	def methodMissing(String name, args) {
		addPhase(name, args)
	}
	
	def addPhase(name, args) {
		mapping.phases.add new LifecyclePhase(name: name.replace('_', '-'),
			goals: ([] + args).flatten())
	}
	
	static LifecycleMapping build(ArtifactType type, Closure buildIt = {}){
		LifecycleMappingBuilder builder = new LifecycleMappingBuilder(type)
		buildIt(builder)
		return builder.mapping
	}
	
	/* default phases */
	
	/**
	 * validate the project is correct and all necessary information is available.
	 */
	def validate(args) {
		addPhase('validate', args)
	}
	/**
	 * initialize build state, e.g. set properties or create directories.
	 */
	def initialize(args) {
		addPhase('initialize', args)
	}
	/**
	 * generate any source code for inclusion in compilation.
	 */
	def generate_sources(args) {
		addPhase('generate-sources', args)
	}
	/**
	 * process the source code, for example to filter any values.
	 */
	def process_sources(args) {
		addPhase('process-sources', args)
	}
	/**
	 * generate resources for inclusion in the package.
	 */
	def generate_resources(args) {
		addPhase('generate-resources', args)
	}
	/**
	 * copy and process the resources into the destination directory, ready for packaging.
	 */
	def process_resources(args) {
		addPhase('process-resources', args)
	}
	/**
	 * compile the source code of the project.
	 */
	def compile(args) {
		addPhase('compile', args)
	}
	/**
	 * post-process the generated files from compilation, for example to do bytecode enhancement on Java classes.
	 */
	def process_classes(args) {
		addPhase('process-classes', args)
	}
	/**
	 * generate any test source code for inclusion in compilation.
	 */
	def generate_test_sources(args) {
		addPhase('generate-test-sources', args)
	}
	/**
	 * process the test source code, for example to filter any values.
	 */
	def process_test_sources(args) {
		addPhase('process-test-sources', args)
	}
	/**
	 * create resources for testing.
	 */
	def generate_test_resources(args) {
		addPhase('generate-test-resources', args)
	}
	/**
	 * copy and process the resources into the test destination directory.
	 */
	def process_test_resources(args) {
		addPhase('process-test-resources', args)
	}
	/**
	 * compile the test source code into the test destination directory
	 */
	def test_compile(args) {
		addPhase('test-compile', args)
	}
	/**
	 * post-process the generated files from test compilation, for example to do bytecode enhancement on Java classes. For Maven 2.0.5 and above.
	 */
	def process_test_classes(args) {
		addPhase('process-test-classes', args)
	}
	/**
	 * run tests using a suitable unit testing framework. These tests should not require the code be packaged or deployed.
	 */
	def test(args) {
		addPhase('test', args)
	}
	/**
	 * perform any operations necessary to prepare a package before the actual packaging. This often results in an unpacked, processed version of the package. (Maven 2.1 and above)
	 */
	def prepare_package(args) {
		addPhase('prepare-package', args)
	}
	/**
	 * take the compiled code and package it in its distributable format, such as a JAR.
	 */
	def _package(args) {
		addPhase('package', args)
	}
	/**
	 * perform actions required before integration tests are executed. This may involve things such as setting up the required environment.
	 */
	def pre_integration_test(args) {
		addPhase('pre-integration-test', args)
	}
	/**
	 * process and deploy the package if necessary into an environment where integration tests can be run.
	 */
	def integration_test(args) {
		addPhase('integration-test', args)
	}
	/**
	 * perform actions required after integration tests have been executed. This may including cleaning up the environment.
	 */
	def post_integration_test(args) {
		addPhase('post-integration-test', args)
	}
	/**
	 * run any checks to verify the package is valid and meets quality criteria.
	 */
	def verify(args) {
		addPhase('verify', args)
	}
	/**
	 * install the package into the local repository, for use as a dependency in other projects locally.
	 */
	def install(args) {
		addPhase('install', args)
	}
	/**
	 * done in an integration or release environment, copies the final package to the remote repository for sharing with other developers and p
	 */
	def deploy(args) {
		addPhase('deploy', args)
	}
}
