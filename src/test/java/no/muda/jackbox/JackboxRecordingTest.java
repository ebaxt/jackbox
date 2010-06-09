package no.muda.jackbox;

import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.Method;

import no.muda.jackbox.example.ExampleDependency;
import no.muda.jackbox.example.ExampleRecordedObject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JackboxRecordingTest {

    private static ClassLoader originalClassLoader;

    @BeforeClass
    public static void setupClassloader() {
        originalClassLoader = Thread.currentThread().getContextClassLoader();
        // TODO: Runtime weaving!!!
        //Thread.currentThread().setContextClassLoader(new RecordingClassLoader());
    }

    @AfterClass
    public static void resetClassloader() {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void shouldRecordMethodCall() throws Exception {
        ExampleRecordedObject recordedObject = new ExampleRecordedObject();
        int actualReturnedValue = recordedObject.exampleMethod(2, 3);

        MethodRecording recording = JackboxRecorder.getLastCompletedRecording();

        assertThat(recording.getMethod().getName()).isEqualTo("exampleMethod");
        assertThat(recording.getArguments()).containsOnly(2, 3);
        assertThat(recording.getRecordedResult()).isEqualTo(actualReturnedValue);
    }

    @Test
    public void shouldRecordDependency() throws Exception {
        ExampleDependency exampleDependency = new ExampleDependency();
        ExampleRecordedObject recordedObject = new ExampleRecordedObject();
        recordedObject.setDependency(exampleDependency);

        String delegatedArgument = "abcd";
        recordedObject.exampleMethodThatDelegatesToDependency(delegatedArgument);

        MethodRecording recording = JackboxRecorder.getLastCompletedRecording();


        Method invokedMethodOnDependency = ExampleDependency.class.getMethod("invokedMethodOnDependency", String.class);
        MethodRecording dependentRecording =
            recording.getDependencyMethodRecording(invokedMethodOnDependency);

        assertThat(dependentRecording.getArguments()).containsOnly(delegatedArgument);
        assertThat(dependentRecording.getRecordedResult()).isEqualTo("ABCD");
    }


}
