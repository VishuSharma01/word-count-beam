package org.apache.beam.examples;

import org.apache.beam.examples.common.ExampleUtils;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;


/**
 * <p>Concepts:
 *
 * <pre>
 *   1. Reading data from text files
 *   2. Specifying 'inline' transforms
 *   3. Counting items in a PCollection
 *   4. Writing data to text files
 * </pre>
 *
 */
public class TestWordCount {

  public interface WordCountOptions extends PipelineOptions {

    /**
     * By default, this example reads from a public dataset containing the text of
     * King Lear. Set this option to choose a different input file or glob.
     */
    @Description("Path of the file to read from")
    @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
    String getInputFile();
    void setInputFile(String value);

    /**
     * Set this required option to specify where to write the output.
     */
    @Description("Path of the file to write to")
    @Required
    String getOutput();
    void setOutput(String value);
  }

  public static void main(String[] args) {
    // Create a PipelineOptions object.
    WordCountOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
			  .as(WordCountOptions.class);

    // Create the Pipeline object .
    Pipeline p = Pipeline.create(options);

    //customer name
     Pipeline p1 = Pipeline.create(options);

     //address
      Pipeline p2 = Pipeline.create(options);

    // Apply the pipeline's transforms.

    // Concept #1: Apply a root transform to the pipeline; 

    p.apply(TextIO.read().from(options.getInputFile()))

     // Concept #2: Apply a ParDo transform to PCollection of text lines.
     .apply("ExtractWords", ParDo.of(new DoFn<String, String>() {
                       @ProcessElement
                       public void processElement(ProcessContext c) {
                         for (String word : c.element().split(ExampleUtils.TOKENIZER_PATTERN)) {
                           if (!word.isEmpty()) {
                             c.output(word);
                           }
                         }
                       }
                     }))

     // Concept #3: Apply the Count transform to  PCollection of individual words.
     .apply(Count.<String>perElement())

     // Apply a MapElements transform that formats  PCollection of word counts into a printable
     // string, suitable for writing to an output file.
     .apply("FormatResults", MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
                       @Override
                       public String apply(KV<String, Long> input) {
                         return input.getKey() + ": " + input.getValue();
                       }
                     }))

     // Concept #4: Apply a write transform, TextIO.Write
     .apply(TextIO.write().to(options.getOutput()));

    // Run the pipeline.
    p.run().waitUntilFinish();
  }
}
