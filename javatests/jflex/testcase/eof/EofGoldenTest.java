// test: eof

package jflex.testcase.eof;

import java.io.File;
import jflex.testing.testsuite.golden.AbstractGoldenTest;
import jflex.testing.testsuite.golden.GoldenInOutFilePair;
import jflex.util.scanner.ScannerFactory;
import org.junit.Test;

/**
 * test %eof and %eofthrows directives https://github.com/jflex-de/jflex/issues/743
 *
 * <p>Note: This test was generated from {@code jflex-testsuite-maven-plugin} test cases. The test
 * relies on golden files for testing, expecting the scanner to output logs on the {@code
 * System.out}. Please migrate to proper unit tests, as describe in <a
 * href="https://github.com/jflex-de/jflex/tree/master/javatests/jflex/testcase">
 * //javatest/jflex/testcase</a>.
 */
// TODO Migrate this test to proper unit tests.
public class EofGoldenTest extends AbstractGoldenTest {

  /** Creates a scanner conforming to the {@code eof.flex} specification. */
  private final ScannerFactory<Eof> scannerFactory = ScannerFactory.of(Eof::new);

  private final File testRuntimeDir = new File("javatests/jflex/testcase/eof");

  @Test
  public void goldenTest0() throws Exception {
    GoldenInOutFilePair golden =
        new GoldenInOutFilePair(
            new File(testRuntimeDir, "eof-0.input"), new File(testRuntimeDir, "eof-0.output"));
    compareSystemOutWith(golden);

    Eof scanner = scannerFactory.createScannerForFile(golden.inputFile);
    while (!scanner.yyatEOF()) {
      System.out.println(scanner.yylex());
    }
  }
}
