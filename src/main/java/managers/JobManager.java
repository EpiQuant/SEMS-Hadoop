package managers;

// import assets.DoubleArrayWritable;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class JobManager {
    public static class TokenizerMapper extends Mapper<Object, Text, IntWritable, DoubleWritable[]>{
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            BufferedReader buff = new BufferedReader(new StringReader(value.toString()));
            
            IntWritable rank = new IntWritable();
            Random r = new Random();
            rank.set(r.nextInt(4));

            String[] tokens;
            String line;
            DoubleWritable[] values = new DoubleWritable[1];
            while ((line = buff.readLine()) != null) {
                tokens = line.split("\\t");
                values = new DoubleWritable[tokens.length - 1];
                for (int i = 1; i < tokens.length; i++) {
                    values[i - 1].set(Double.parseDouble(tokens[i]));
                }
            }
            context.write(rank, values);
        }
    }

    public static class IntSumReducer extends Reducer<IntWritable,DoubleWritable[],IntWritable,DoubleWritable[]> {
        public void reduce(IntWritable key, DoubleWritable[] values, Context context) throws IOException, InterruptedException {
            // int sum = 0;
            // for (IntWritable val : values) {
            //   sum += val.get();
            // }
            // result.set(sum);
            context.write(key, values);
        }
    }

    public void run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(JobManager.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path("output"));
        job.waitForCompletion(true);
    }
}