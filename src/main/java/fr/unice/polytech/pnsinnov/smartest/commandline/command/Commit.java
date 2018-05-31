package fr.unice.polytech.pnsinnov.smartest.commandline.command;

import fr.unice.polytech.pnsinnov.smartest.commandline.Command;
import fr.unice.polytech.pnsinnov.smartest.parser.Parser;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@CommandLine.Command(name = "commit", description = "Run tests then record changes to the repository.")
public class Commit extends Command {
    @CommandLine.Option(names = {"-m", "--message"}, required = true, description = "Use the given <msg> as the " +
            "commit message. If multiple -m options are given, their values are concatenated as separate paragraphs.")

    private String message;

    public void run() {
        Parser parser = new Parser(context.getPlugin().getExplorerStrategy().getPathToSrc(""), context.getPlugin().getExplorerStrategy().getPathToTest(""));

        parser.sourceCodeParsing();
        parser.testsParsing();

        Set<String> toRun = new HashSet<>();

        Git git = null;

        try {
            git = Git.open(new File(Paths.get("").toAbsolutePath().toString(), ".git"));

            Repository repository = git.getRepository();

            ObjectId lastCommitId = repository.resolve(Constants.HEAD);

            RevWalk revWalk = new RevWalk(repository);
            RevCommit commit = revWalk.parseCommit(lastCommitId);

            for (String path : git.status().call().getUncommittedChanges()) {
                if(context.getPlugin().isValidPath(path)) {
                    toRun.addAll(context.getPlugin().compareChanges(path, getContent(git, commit, path)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        } finally {
            if (git != null){
                git.close();
            }
        }

        if(context.getPlugin().getTestRunner().runAllTests(toRun)){
            this.commitAllChanges();
            context.out().println("Les tests sont passés, le code a été commit");
        } else {
            context.out().println("Les tests ont échoués");
        }

    }

    private String getContent(Git git, RevCommit commit, String path) throws IOException {
        try (TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
            ObjectId blobId = treeWalk.getObjectId(0);
            try (ObjectReader objectReader = git.getRepository().newObjectReader()) {
                ObjectLoader objectLoader = objectReader.open(blobId);
                byte[] bytes = objectLoader.getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (NullPointerException e){
            return null;
        }
    }

    private void commitAllChanges() {
        Git git = null;

        try {
            git = Git.open(new File(Paths.get("").toAbsolutePath().toString(), ".git"));
            AddCommand add = git.add();

            for (String path : git.status().call().getUncommittedChanges()) {
                add.addFilepattern(path);
            }

            add.call();
            git.commit().setMessage(message).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            context.out().println("The \".git\" was not found in this folder");
        } finally {
            if (git != null){
                git.close();
            }
        }
    }
}
