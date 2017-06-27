package codegen;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.plexus.build.incremental.BuildContext;

@Mojo(name = "codegen", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CodeGenMojo extends AbstractMojo {

	@Component
	private BuildContext context;

	@Parameter(property = "configFile", required = true)
	private File configFile;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/")
	private File srcDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Running CodeGen...");
		if (!srcDirectory.exists()) {
			throw new MojoExecutionException(String.format("%s does not exist", srcDirectory.getName()));
		}

		if (!srcDirectory.isDirectory()) {
			throw new MojoExecutionException(String.format("%s is not a directory", srcDirectory.getName()));
		}
		final List<File> files = listFilesForFolder(srcDirectory);
		final List<File> filesForProcessing = getFilesForProcessing(files);
		if (filesForProcessing.isEmpty()) {
			getLog().info("No files have changed! Not processing...");
			return;
		}
		List<File> parsed;
		try {
			final CodeGenerator generator = new CodeGenerator(configFile);
			parsed = generator.parse(filesForProcessing);
		} catch (final ParseExceptions pe) {
			throw new MojoFailureException("Couldn't parse files: " + pe.getExceptions(), pe);
		} catch (final Exception e) {
			throw new MojoFailureException("Couldn't process files", e);
		}
		printParsedFiles(parsed);
	}

	private List<File> getFilesForProcessing(List<File> files) {
		final List<File> filesForProcessing = new ArrayList<File>();
		if (context.isIncremental()) {
			if (context.hasDelta(configFile)) {
				getLog().info("Config file " + configFile + " has been changed, refreshing files...");
				filesForProcessing.addAll(listFiles(files));
			} else {
				filesForProcessing.addAll(filterFilesOnBuildContext(files));
			}

		} else {
			filesForProcessing.addAll(listFiles(files));
		}
		return filesForProcessing;
	}

	private Collection<? extends File> listFiles(List<File> files) {
		for (File file : files) {
			getLog().info("Processing " + file);
		}
		return files;
	}

	private void printParsedFiles(List<File> parsed) {
		for (File file : parsed) {
			getLog().info("Processed " + file);
			context.refresh(file);
		}
	}

	private List<File> filterFilesOnBuildContext(List<File> files) {
		List<File> filesForProcessing = new ArrayList<File>();
		for (File file : files) {
			if (context.hasDelta(file)) {
				getLog().info("Processing " + file);
				filesForProcessing.add(file);
			}
		}
		return filesForProcessing;
	}

	private List<File> listFilesForFolder(File srcDirectory2) {
		if (!srcDirectory.isDirectory()) {
			throw new IllegalArgumentException("argument is not a directory");
		}
		List<File> files = new ArrayList<File>();
		for (File file : srcDirectory2.listFiles(getFileFilter())) {
			if (file.isDirectory()) {
				files.addAll(listFilesForFolder(file));
			} else {
				getLog().debug("Adding file to content " + file);
				files.add(file);
			}
		}
		return files;
	}

	private FileFilter getFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				if (pathname.getName().endsWith(".java")) {
					return true;
				}
				return false;
			}
		};
	}

}
