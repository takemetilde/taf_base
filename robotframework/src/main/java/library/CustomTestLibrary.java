package library;

import com.github.markusbernhardt.selenium2library.RunOnFailureKeywordsAdapter;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import static org.testng.Assert.*;

/**
 * This custom library is being used as an example of how to write methods that correlate with keywords being used in your robot scripts.  The Robot Framework will automatically parse the method name into the keywords by camel case or underscore.  The @RobotKeyword annotation is used to identify blocks of code to be identified by Robot.
 * @RobotKeywords is used to identify custom libraries.
 * @RobotKeyword is used to identify keywords.
 * @ArgumentNames is used for identifying the arguments.
 * The directory of this library is used in the robot scripts themselves for importing libraries, i.e. library.CustomTestLbrary
 */
@RobotKeywords
public class CustomTestLibrary extends RunOnFailureKeywordsAdapter {

    /**
     * This method uses TestNG to assert that the two string arguments are not equal.
     * @param strPrint
     * @param strPrint2
     */
    @RobotKeyword
    @ArgumentNames({"strPrint", "strPrint2"})
    public void printTestKeywordNotEquals(String strPrint, String strPrint2) {
        assertNotEquals(strPrint, strPrint2);
    }

    /**
     * This method uses TestNG to assert that the two string arguments are equal.
     * @param strPrint
     * @param strPrint2
     */
    @RobotKeyword
    @ArgumentNames({"strPrint", "strPrint2"})
    public void print_test_keyword_equals(String strPrint, String strPrint2) {
        assertEquals(strPrint, strPrint2);
    }
}
