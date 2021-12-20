package gov.sandia.watchr;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;

import gov.sandia.watchr.model.JenkinsConfigContext;
import hudson.model.Job;

public class WatchrJenkinsAppManagerTest {
    
    private final Job<?,?> job = Mockito.mock(Job.class);

    @Test
    public void testGetConfigContext_Blank() {
        
        JenkinsConfigContext context = WatchrJenkinsApp.getConfigContextOrDefault(job);
        assertNotNull(context);
    }
}
