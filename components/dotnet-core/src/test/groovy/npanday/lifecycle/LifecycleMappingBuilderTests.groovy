package npanday.lifecycle;

import npanday.ArtifactType;
import static LifecycleMappingBuilder.build as forType

import org.junit.Test 

class LifecycleMappingBuilderTests {
	
	@Test
	void construct() {
		new LifecycleMappingBuilder()
	}
	
	@Test
	void createEmptyMapping() {
		def result = LifecycleMappingBuilder.build(ArtifactType.DOTNET_LIBRARY)
		assert result.class == LifecycleMapping
		assert result.type == ArtifactType.DOTNET_LIBRARY
	}
	
	@Test
	void createMappingWithOnePhase() {
		def result = LifecycleMappingBuilder.build(ArtifactType.DOTNET_LIBRARY){
				it.phasename()
			}
		assert result.phases.size() == 1
		assert result.phases[0].name == "phasename"
	}
	
	@Test
	void createMappingWithOneDashedPhase() {
		def result = LifecycleMappingBuilder.build(ArtifactType.DOTNET_LIBRARY){
			it.phase_name()
		}
		assert result.phases.size() == 1
		assert result.phases[0].name == "phase-name"
	}
	
	@Test
	void createMappingWithPackagePhase2() {
		def result = LifecycleMappingBuilder.build(ArtifactType.DOTNET_LIBRARY){
			LifecycleMappingBuilder b->
			
			b.package()
		}
		assert result.phases.size() == 1
		assert result.phases[0].name == "package"
	}
	
	@Test
	void createMappingWithPredefined_pre_integration_test() {
		def result = forType(ArtifactType.DOTNET_LIBRARY){
			LifecycleMappingBuilder b->
			
			b.pre_integration_test()
		}
		assert result.phases.size() == 1
		assert result.phases[0].name == "pre-integration-test"
	}
	
	@Test
	void singleGoal() {
		def result = forType(ArtifactType.DOTNET_LIBRARY){
			LifecycleMappingBuilder b->
			
			b.x('goal')
		}
		assert result.phases[0].goals.size() == 1
		assert result.phases[0].goals[0] == 'goal'
	}
	
	@Test
	void twoGoalsAsArgs() {
		def result = forType(ArtifactType.DOTNET_LIBRARY){
			LifecycleMappingBuilder b->
			
			b.x 'a', 'b'
		}
		assert result.phases[0].goals == ['a', 'b']
	}
	
	@Test
	void twoGoalsAsList() {
		def result = forType(ArtifactType.DOTNET_LIBRARY){
			LifecycleMappingBuilder b->
			
			b.x (['a', 'b'])
		}
		assert result.phases[0].goals == ['a', 'b']
	}
	
	@Test
	void fiveGoalsMixed() {
		def result = forType(ArtifactType.DOTNET_LIBRARY){
			LifecycleMappingBuilder b->
			
			b.x (['a', 'b'], 'c', ['d', 'e'])
		}
		assert result.phases[0].goals == ['a', 'b', 'c', 'd', 'e']
	}
}
