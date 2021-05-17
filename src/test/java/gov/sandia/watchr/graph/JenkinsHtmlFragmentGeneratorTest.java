package gov.sandia.watchr.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import gov.sandia.watchr.model.JenkinsConfigContext;

public class JenkinsHtmlFragmentGeneratorTest {

    private JenkinsHtmlFragmentGenerator fragmentGenerator;

    @Before
    public void setup() {
        fragmentGenerator = new JenkinsHtmlFragmentGenerator();
    }

    @Test
    public void testBuildCategoryOption() {        
        String expected =
            "<tr><td class='setting-name'>Category:  " +
            "</td><td class='setting-main'><select class='setting-input dropdownList' name='category' " +
            "style='width: 200px;'><option value='a' selected='selected'>A</option><option value='b'>B</option>" +
            "</select></td></tr>";
        Set<String> actualList = new LinkedHashSet<>();
        actualList.add("A");
        actualList.add("B");
        String actual = fragmentGenerator.buildCategoryOption(actualList, "A");
        assertEquals(expected, actual);
    }

    @Test
    public void testBuildRangeToDisplayOption() {        
        String expected =
            "<tr><td class='setting-name' style='vertical-align:middle;'>" +
            "Range to Display: </td><td><input class='setting-input' name='timeScale' type='text' value='30' style='width: 200px;'>" +
            "</input></td></tr>";
        String actual = fragmentGenerator.buildRangeToDisplayOption(30);
        assertEquals(expected, actual);
    }

    @Test
    public void testBuildGraphWidthOption() {        
        String expected =
            "<tr><td class='setting-name' style='vertical-align:middle;'>" +
            "Graph Width: </td><td><input class='setting-input' name='graphWidth' type='text' value='300' style='width: 200px;'></input></td>" +
            "</tr>";
        String actual = fragmentGenerator.buildGraphWidthOption(300);
        assertEquals(expected, actual);
    }

    @Test
    public void testBuildGraphHeightOption() {        
        String expected =
            "<tr><td class='setting-name' style='vertical-align:middle;'>" +
            "Graph Height: </td><td><input class='setting-input' name='graphHeight' type='text' value='300' style='width: 200px;'>" +
            "</input></td></tr>";
        String actual = fragmentGenerator.buildGraphHeightOption(300);
        assertEquals(expected, actual);
    }

    @Test
    public void testBuildGraphsPerRowOption() {        
        String expected =
            "<tr><td class='setting-name' style='vertical-align:middle;'>Graphs Per Row: </td>" +
            "<td><input class='setting-input' name='graphsPerRow' type='text' value='3' style='width: 200px;'></input></td></tr>";
        String actual = fragmentGenerator.buildGraphsPerRowOption(3);
        assertEquals(expected, actual);
    }

    @Test
    public void testBuildRoundToOption() {        
        String expected =
            "<tr><td class='setting-name' style='vertical-align:middle;'>" +
            "Number of Displayed Decimal Places: </td><td><input class='setting-input' name='roundTo' type='text' value='3' " +
            "style='width: 200px;'></input></td></tr>";
        String actual = fragmentGenerator.buildRoundToOption(3);
        assertEquals(expected, actual);
    }

    @Test
    public void testBuildMenuBarTable() {
        String expected =
            "<div class='pane-frame'><table id='graphSettings' class='pane' style='margin-top:0; border-top:none'>" +
            "<tr style='border-top:none; white-space: normal'><td>contents</td></tr></table></div>";
        String actual = fragmentGenerator.buildMenuBarTable("contents");
        assertEquals(expected, actual);
    }

    @Test
    public void testNoGraphs() {
        String expected = "<div class='error' style='margin:15px;'>No graphs were found at this location for the given category.</div>";
        String actual = fragmentGenerator.noGraphs();
        assertEquals(expected, actual);
    }

    
    @Test
    public void testBuildOptionsForm() {
        JenkinsConfigContext context = getDummyConfigContext();
        StringBuilder expectedSb = new StringBuilder();
        expectedSb.append("<form name='frmOptions' method='post' autocomplete='off'>");
        expectedSb.append("<input id='path' name='path' type='hidden' value='root'></input>");
        expectedSb.append("<input id='Jenkins-Crumb' name='Jenkins-Crumb' type='hidden'></input>");
        expectedSb.append("<input id='crumbRequestField' name='crumbRequestField' type='hidden'></input>");
        expectedSb.append("<table width='100%'>");
        expectedSb.append("<tr><td class='setting-name'>Category:  </td><td class='setting-main'><select class='setting-input dropdownList' name='category' style='width: 200px;'></select></td></tr>");
        expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Range to Display: </td><td><input class='setting-input' name='timeScale' type='text' value='30' style='width: 200px;'></input></td></tr>");
        expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Graph Width: </td><td><input class='setting-input' name='graphWidth' type='text' value='500' style='width: 200px;'></input></td></tr>");
        expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Graph Height: </td><td><input class='setting-input' name='graphHeight' type='text' value='500' style='width: 200px;'></input></td></tr>");
        expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Graphs Per Row: </td><td><input class='setting-input' name='graphsPerRow' type='text' value='3' style='width: 200px;'></input></td></tr>");
        expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Number of Displayed Decimal Places: </td><td><input class='setting-input' name='roundTo' type='text' value='3' style='width: 200px;'></input></td></tr>");
        expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Sort ascending: </td><td class='setting-main'><input class='setting-input' name='sortAscending' type='checkbox' value='true' style='width: 10px' checked></input></td></tr>");
        expectedSb.append("<tr><td><input class='submit-button primary' name='Submit' type='submit' value='submit'></input></td></tr>");
        expectedSb.append("</table></form>");
        String actual = fragmentGenerator.buildOptionsForm(context);
        assertEquals(expectedSb.toString(), actual);
    }

    @Test
    public void testBuildMenuBar() {
        try {
            JenkinsConfigContext context = getDummyConfigContext();
            StringBuilder expectedSb = new StringBuilder();
            expectedSb.append("<div class='pane-frame'><table id='graphSettings' class='pane' style='margin-top:0; border-top:none'>");
            expectedSb.append("<tr style='border-top:none; white-space: normal'><td><h2 style='padding-top:20px;padding-left:20px;'>Performance Report Display Configuration</h2>");
            expectedSb.append("<form name='frmOptions' method='post' autocomplete='off'><input id='path' name='path' type='hidden' value='root'></input>");
            expectedSb.append("<input id='Jenkins-Crumb' name='Jenkins-Crumb' type='hidden'></input>");
            expectedSb.append("<input id='crumbRequestField' name='crumbRequestField' type='hidden'></input>");
            expectedSb.append("<table width='100%'>");
            expectedSb.append("<tr><td class='setting-name'>Category:  </td><td class='setting-main'><select class='setting-input dropdownList' name='category' style='width: 200px;'></select></td></tr>");
            expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Range to Display: </td><td><input class='setting-input' name='timeScale' type='text' value='30' style='width: 200px;'></input></td></tr>");
            expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Graph Width: </td><td><input class='setting-input' name='graphWidth' type='text' value='500' style='width: 200px;'></input></td></tr>");
            expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Graph Height: </td><td><input class='setting-input' name='graphHeight' type='text' value='500' style='width: 200px;'></input></td></tr>");
            expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Graphs Per Row: </td><td><input class='setting-input' name='graphsPerRow' type='text' value='3' style='width: 200px;'></input></td></tr>");
            expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Number of Displayed Decimal Places: </td><td><input class='setting-input' name='roundTo' type='text' value='3' style='width: 200px;'></input></td></tr>");
            expectedSb.append("<tr><td class='setting-name' style='vertical-align:middle;'>Sort ascending: </td><td class='setting-main'><input class='setting-input' name='sortAscending' type='checkbox' value='true' style='width: 10px' checked></input></td></tr>");
            expectedSb.append("<tr><td><input class='submit-button primary' name='Submit' type='submit' value='submit'></input></td></tr>");
            expectedSb.append("</table></form></td></tr></table></div><br><p style='border-bottom: 1px #DDDDDD solid;margin-bottom: 5px;margin-left: 10px;font-size: 14pt;font-weight: bold;font-variant: small-caps'>Pages</p><p style='margin-left:10px'><strong>1</strong></p>");
            String actual = fragmentGenerator.buildMenuBar(context, -1);
            assertEquals(expectedSb.toString(), actual);
        } catch(UnsupportedEncodingException e) {
            fail(e.getMessage());
        }
    }

    private JenkinsConfigContext getDummyConfigContext() {
        try {
            File emptyDir = Files.createTempDirectory(null).toFile();
            String TEST_DATABASE = "MyDatabase";

            return new JenkinsConfigContext(TEST_DATABASE, emptyDir);
        } catch(IOException e) {
            fail(e.getMessage());
        }
        return null;
    }
}
