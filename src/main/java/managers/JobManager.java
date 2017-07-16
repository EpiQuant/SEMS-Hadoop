package managers;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class JobManager {
    public static class JobSplitMapper extends Mapper<Object, Text, Text, Text>{
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            BufferedReader buff = new BufferedReader(new StringReader(value.toString()));
            Random r = new Random();
            String[] tokens;
            String line;

			while ((line = buff.readLine()) != null) {
                tokens = line.split("\\t");
                String output = tokens[1];
                for (int i = 2; i < tokens.length; i++) {
                    output += "," + tokens[i];
                }
                if (tokens.length == 2) {
                    context.write(new Text("0"), new Text(output));
                } else {
                    context.write(new Text("1:" + r.nextInt(10)), new Text(output));
                }
			}
        }
    }

    public static class LinRegReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Text values, Context context) throws IOException, InterruptedException {
            context.write(key, values);
        }
    }

    public void run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "job manager");
        job.setJarByClass(JobManager.class);
        
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(JobSplitMapper.class);
        job.setCombinerClass(LinRegReducer.class);
        job.setReducerClass(LinRegReducer.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path("output"));
        
        job.waitForCompletion(true);
    }
}