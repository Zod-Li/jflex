package jflex.ucd_generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jflex.ucd_generator.model.UnicodeData;
import jflex.ucd_generator.scanner.BinaryPropertiesFileScanner;
import jflex.ucd_generator.scanner.DerivedAgeScanner;
import jflex.ucd_generator.scanner.EnumeratedPropertyFileScanner;
import jflex.ucd_generator.scanner.PropertyAliasesScanner;
import jflex.ucd_generator.scanner.PropertyValueAliasesScanner;
import jflex.ucd_generator.scanner.ScriptExtensionsScanner;
import jflex.ucd_generator.scanner.UcdScannerException;
import jflex.ucd_generator.scanner.UnicodeDataScanner;
import jflex.ucd_generator.ucd.UcdFileType;
import jflex.ucd_generator.ucd.UcdVersion;

public class UcdScanner {

  private static final boolean DEBUG = false;

  private final UcdVersion ucdVersion;
  final UnicodeData unicodeData;

  UcdScanner(UcdVersion ucdVersion) {
    this.ucdVersion = ucdVersion;
    this.unicodeData = new UnicodeData(ucdVersion.version());
  }

  /** Scans all UCD data files. */
  public UnicodeData scan() throws UcdScannerException {
    try {
      scanPropertyAliases();
      scanPropertyValueAliases();
      scanUnicodeData();
      scanPropList();
      scanDerivedCoreProperties();
      scanScripts();
      scanScriptExtensions();
      scanBlocks();
      scanLineBreak();
      scanGraphemeBreakProperty();
      scanSentenceBreakProperty();
      scanWordBreakProperty();
      scanDerivedAge();
      unicodeData.addCompatibilityProperties();

      return unicodeData;
    } catch (Throwable thr) {
      String cause = (thr.getMessage() != null) ? thr.getMessage() : "Unknown error";
      throw new UcdScannerException(
          "Failed to emit Unicode properties for version " + ucdVersion.version() + " : " + cause,
          thr);
    }
  }

  void scanPropertyAliases() throws IOException {
    File file = ucdVersion.getFile(UcdFileType.PropertyAliases);
    if (file != null) {
      assertFileExists(file);
      PropertyAliasesScanner scanner =
          new PropertyAliasesScanner(Files.newReader(file, StandardCharsets.UTF_8), unicodeData);
      scanner.scan();
    }
  }

  void scanPropertyValueAliases() throws IOException {
    File file = ucdVersion.getFile(UcdFileType.PropertyValueAliases);
    if (file != null) {
      assertFileExists(file);
      PropertyValueAliasesScanner scanner =
          new PropertyValueAliasesScanner(
              Files.newReader(file, StandardCharsets.UTF_8), unicodeData);
      scanner.scan();
    }
  }

  void scanUnicodeData() throws IOException {
    File file = ucdVersion.getFile(UcdFileType.UnicodeData);
    Preconditions.checkNotNull(file, "UnicodeData.txt not defined in UCD %s", ucdVersion);
    assertFileExists(file);
    UnicodeDataScanner scanner =
        new UnicodeDataScanner(
            Files.newReader(file, StandardCharsets.UTF_8), ucdVersion, unicodeData);
    scanner.scan();
  }

  void scanPropList() throws IOException {
    scanBinaryProperties(unicodeData, ucdVersion.getFile(UcdFileType.PropList));
  }

  void scanDerivedCoreProperties() throws IOException {
    scanBinaryProperties(unicodeData, ucdVersion.getFile(UcdFileType.DerivedCoreProperties));
  }

  void scanScripts() throws IOException {
    scanEnumeratedProperty(
        unicodeData,
        ucdVersion.getFile(UcdFileType.Scripts),
        /*defaultPropertyName=*/ "Script",
        "Unknown");
  }

  void scanScriptExtensions() throws IOException {
    File file = ucdVersion.getFile(UcdFileType.ScriptExtensions);
    if (file != null) {
      assertFileExists(file);
      ScriptExtensionsScanner scanner =
          new ScriptExtensionsScanner(Files.newReader(file, StandardCharsets.UTF_8), unicodeData);
      scanner.scan();
    }
  }

  void scanBlocks() throws IOException {
    scanEnumeratedProperty(
        unicodeData,
        ucdVersion.getFile(UcdFileType.Blocks),
        /*defaultPropertyName=*/ "Block",
        "No_Block");
  }

  void scanLineBreak() throws IOException {
    scanEnumeratedProperty(
        unicodeData,
        ucdVersion.getFile(UcdFileType.LineBreak),
        /*defaultPropertyName=*/ "Line_Break",
        "XX");
  }

  void scanGraphemeBreakProperty() throws IOException {
    scanEnumeratedProperty(
        unicodeData,
        ucdVersion.getFile(UcdFileType.GraphemeBreakProperty),
        "Grapheme_Cluster_Break",
        "Other");
  }

  void scanSentenceBreakProperty() throws IOException {
    scanEnumeratedProperty(
        unicodeData,
        ucdVersion.getFile(UcdFileType.SentenceBreakProperty),
        "Sentence_Break",
        "Other");
  }

  void scanWordBreakProperty() throws IOException {
    scanEnumeratedProperty(
        unicodeData, ucdVersion.getFile(UcdFileType.WordBreakProperty), "Word_break", "Other");
  }

  void scanDerivedAge() throws IOException {
    File file = ucdVersion.getFile(UcdFileType.DerivedAge);
    if (file != null) {
      assertFileExists(file);
      DerivedAgeScanner scanner =
          new DerivedAgeScanner(Files.newReader(file, StandardCharsets.UTF_8), unicodeData, "Age");
      scanner.scan();
    }
  }

  /** Scans any binary properties file. */
  private static void scanBinaryProperties(UnicodeData unicodeData, File file) throws IOException {
    if (file != null) {
      assertFileExists(file);
      BinaryPropertiesFileScanner scanner =
          new BinaryPropertiesFileScanner(
              Files.newReader(file, StandardCharsets.UTF_8), unicodeData);
      scanner.scan();
    }
  }

  /** Scans any enumerated properties file. */
  private static void scanEnumeratedProperty(
      UnicodeData unicodeData, File file, String defaultPropertyName, String defaultPropertyValue)
      throws IOException {
    if (file != null) {
      assertFileExists(file);
      EnumeratedPropertyFileScanner scanner =
          new EnumeratedPropertyFileScanner(
              Files.newReader(file, StandardCharsets.UTF_8),
              unicodeData,
              defaultPropertyName,
              defaultPropertyValue);
      ImmutableSet<String> before =
          DEBUG ? ImmutableSet.copyOf(unicodeData.propertyValues()) : ImmutableSet.of();
      scanner.scan();
      if (DEBUG) {
        SetView<String> diff =
            Sets.difference(ImmutableSet.copyOf(unicodeData.propertyValues()), before);
        System.out.println(diff);
      }
    }
  }

  private static void assertFileExists(File file) throws FileNotFoundException {
    if (!file.isFile()) {
      throw new FileNotFoundException(file.getAbsolutePath());
    }
  }

  public UcdVersion ucdVersion() {
    return ucdVersion;
  }
}
