package managers;

import utilities.ConfSet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class DataBuilder {
    public static class TokenMapper extends Mapper<Object, Text, Text, Text>{
        private String pathString;
        private Path path;
        private FileSystem fs;

        @Override
        public void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            pathString = conf.get("path");
            path = new Path(pathString);
            fs = FileSystem.get(conf);
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            BufferedReader fileBuff = new BufferedReader(new InputStreamReader(fs.open(path)));
            String fileLine;

            while ((fileLine = fileBuff.readLine()) != null) {
                BufferedReader valueBuff = new BufferedReader(new StringReader(value.toString()));
                String valueLine;
                while ((valueLine = valueBuff.readLine()) != null) {
                    if (!valueLine.equals(fileLine)) {
                        context.write(new Text(fileLine), new Text(valueLine));
                    }
                }
            }
        }
    }

    public static class ElementReducer extends Reducer<Text, Text, Text, NullWritable>{

        @Override
        public void reduce(Text key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
            for (Text val: value) {
                // String[] fileTokens = key.toString().split("\\t");
                // String[] valueTokens = value.toString().split("\\t");
                // double[] outDoubles = new double[fileTokens.length - 1];
                // for (int i = 0; i < outDoubles.length; i++) {
                //     outDoubles[i] = Double.parseDouble(fileTokens[i + 1]) * Double.parseDouble(valueTokens[i + 1]);
                // }
                // String outString = fileTokens[0] + ":::" + valueTokens[0];
                // for (int i = 0; i < outDoubles.length; i++) {
                //     outString += "\t" + outDoubles[i];
                // }
                context.write(new Text("Hello World"), NullWritable.get());
                // context.write(key, NullWritable.get());
            }
        }
    }

    public Job run(String inputPath, String outputDir) throws Exception {
        Configuration conf = new Configuration();
        conf.set("path", inputPath);
        Job job = Job.getInstance(conf, "data builder");
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputDir));

        job.setJarByClass(DataBuilder.class);

        Configuration chainMapperConf = new Configuration(false);
        ChainMapper.addMapper(job, TokenMapper.class, Object.class, Text.class, Text.class, Text.class, chainMapperConf);

        Configuration chainReducerConf = new Configuration(false);
        ChainReducer.setReducer(job, ElementReducer.class, Text.class, Text.class, Text.class, NullWritable.class, chainReducerConf);

        job.waitForCompletion(true);
        return job;
    }
}