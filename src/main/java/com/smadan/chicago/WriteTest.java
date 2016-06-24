package com.smadan.chicago;

import com.google.common.primitives.Ints;
import com.xjeffrose.chicago.client.ChicagoTSClient;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by root on 6/23/16.
 */
public class WriteTest implements Runnable {
  TestChicagoCluster testChicagoCluster;
  HashMap<String,String> servers = new HashMap<>();
  private final String key = "writeTestKey";
  ChicagoTSClient cts;
  private final CountDownLatch latch;


  public WriteTest(CountDownLatch latch,ChicagoTSClient cts){
    this.latch = latch;
    this.cts=cts;
  }

  //@Before
  //public  void  setup() throws  Exception{
  //  servers.put("s1","10.24.25.188:12000");
  //  servers.put("s2","10.24.33.123:12000");
  //  servers.put("s3","10.24.25.189:12000");
  //  servers.put("s4","10.25.145.56:12000");
  //  String zkString = "10.24.25.188:2181,10.24.25.189:2181,10.24.33.123:2181,10.25.145.56:2181";
  //  cts = new ChicagoTSClient(zkString,3);
  //  cts.startAndWaitForNodes(3);
  //}

  //@Test
  //public void write10Values() throws Exception{
  //  while(true) {
  //    String _v = InetAddress.getLocalHost().getHostAddress() + "val";
  //    byte[] val = _v.getBytes();
  //    assertTrue(Ints.fromByteArray(cts.write(key.getBytes(), val)) > 0);
  //  }
  //}

  @Override
  public void run(){
    try{
      String v = InetAddress.getLocalHost().getHostAddress() + "val";
      int o =Ints.fromByteArray(cts.write(key.getBytes(),v.getBytes()));
      System.out.println(o);
    }catch (Exception e){
      e.printStackTrace();
    }finally{
      latch.countDown();
    }
  }

  public static void main(String[] args) throws Exception {
    final int loop = Integer.parseInt(args[0]);
    final int workerSize = Integer.parseInt(args[1]);
    final int clients = Integer.parseInt(args[2]);
    ExecutorService executor = Executors.newFixedThreadPool(workerSize);
    CountDownLatch latch = new CountDownLatch(loop);
    ChicagoTSClient[] ctsa = new ChicagoTSClient[clients];
    for(int i =0;i<clients;i++){
      ctsa[i] = new ChicagoTSClient("10.24.25.188:2181,10.24.25.189:2181,10.24.33.123:2181,10.25.145.56:2181",3);
      ctsa[i].startAndWaitForNodes(3);
    }

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      executor.submit(new WriteTest(latch,ctsa[i%clients]));
    }
    latch.await();

    System.out.println("Total time taken for "+loop+ " writes ="+ (System.currentTimeMillis() - startTime) + "ms");
    executor.shutdownNow();
    System.exit(0);
  }
}
