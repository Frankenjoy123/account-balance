package com.joey;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Hello world!
 *
 */
public class App 
{

    /**
     * 如果有个账户有2000元
     * 同时扣除1500，600
     * 如何保证线程安全，不能用Lock,Synchronized
     * @param args
     * @throws InterruptedException
     */
    public static void main( String[] args ) throws InterruptedException {
        Balance balance = new Balance(2000);
        int count = 20;

        //发令枪
        CountDownLatch latch = new CountDownLatch(count);

        BlockingQueue blockingQueue = new LinkedBlockingDeque();

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            int finalI = i;
            new Thread(() -> {
                try {

                    try {
                        // 阻塞
                        // count = 0 就会释放所有的共享锁
                        // 万箭齐发
                        latch.await();
                        Thread.sleep(finalI*100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //必然会调用，可能会有很多线程同时去访问getInstance()
//                    Object obj = LazyOne.getInstance();
//                    System.out.println(System.currentTimeMillis() + ":" + obj);
//					if (finalI == 0) {
//						balance.sub(1500);
//					}
                    int v = (int) (Math.random() * 300);
//					if (finalI == 1) {
                    balance.sub(v);
//					}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start(); //每循环一次，就启动一个线程,具有一定的随机性

            //每次启动一个线程，count --
            latch.countDown();

        }
        long end = System.currentTimeMillis();



        Thread.sleep(2000);
        System.out.println("现在金额查询为" + balance.getMoney());

    }


    static class Balance {

        private int money;

        public Balance(int money) {
            this.money = money;
        }

        public int getMoney() {
            return money;
        }

        private BlockingQueue<String> operations = new LinkedBlockingDeque<String>(1) {
            private static final long serialVersionUID = 2853881738977199754L;

            @Override
            public void put(String s) throws InterruptedException {
                super.put(s);
                String[] array = s.split("_");
                //进行加减操作
                if (array[0].contains("add")) {
                    doAdd(Integer.valueOf(array[1]));
                }
                if (array[0].contains("sub")) {
                    doSub(Integer.valueOf(array[1]));
                }
                if (!super.take().equals(s)) {
                    throw new RuntimeException("出现异常");
                }
            }
        };
        public int add(int addMoney) throws InterruptedException {
            operations.put("add_" + addMoney);
            return this.money;
        }
        public int sub(int subMoney) throws InterruptedException {
            operations.put("sub_" + subMoney);

            return this.money;
        }
        private void doSub(int money) {
            int temp = this.money;
            this.money -= money;
            if (this.money < 0) {
                System.err.println("减去" + money + " 还剩" + this.money + "金额不能为负数 操作回滚 " + temp);
                this.money = temp;
            } else {
                System.out.println("减去" + money + " 还剩" + this.money);
            }
        }
        private void doAdd(int money) {
            this.money += money;
        }
    }

    static class Result{
        private int lastAmount;

        private int curAmount;

        private int shouldSub;

        private int realSub;

        private boolean success;

        private String reason;

        public int getLastAmount() {
            return lastAmount;
        }

        public void setLastAmount(int lastAmount) {
            this.lastAmount = lastAmount;
        }

        public int getCurAmount() {
            return curAmount;
        }

        public void setCurAmount(int curAmount) {
            this.curAmount = curAmount;
        }

        public int getShouldSub() {
            return shouldSub;
        }

        public void setShouldSub(int shouldSub) {
            this.shouldSub = shouldSub;
        }

        public int getRealSub() {
            return realSub;
        }

        public void setRealSub(int realSub) {
            this.realSub = realSub;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
