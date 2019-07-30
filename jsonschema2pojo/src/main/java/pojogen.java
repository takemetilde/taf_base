import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo (name = "pojogen")
public class pojogen extends AbstractMojo {

    @Parameter(property = "gen")
    private boolean generate;

    public void execute() throws MojoExecutionException, MojoFailureException {

    }


}
