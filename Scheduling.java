package scheduling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Scheduling {
    static String inputFileAddress = "src/scheduling/input1.txt";
    static String outputFileAddress = "src/scheduling/output1.txt";
    static ArrayList<process> processesList = new ArrayList<>();
    static ArrayList<process> processesListTemp = new ArrayList<>();
    static ArrayList<process> doneProcessesList = new ArrayList<>();
    static int quantum;
    static int countOfProcesses;

    static long TIME =0;

    public static void main(String[] args) {
        String firstLine="";

        try {
            File myObj = new File(inputFileAddress);
            Scanner myReader = new Scanner(myObj);
            //get first line of file = algorithm name
            firstLine = myReader.nextLine();
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        // get all processes
        getProcesses();

        if (firstLine.contains("RR")) {
            // get quantum
            Scanner in = new Scanner(firstLine).useDelimiter("[^0-9]+");
            quantum = in.nextInt();
            RR(quantum);
        } else if (firstLine.contains("SJF")){
            SJF();
        } else if (firstLine.contains("PR_noPREMP")) {
            priorityWithNoPremp();
        } else if (firstLine.contains("PR_withPREMP")) {
            priorityWithPremp();
        }



    }

    static void RR(int Quantum){

        ArrayList<process> readyQueue = new ArrayList<>();

        writeToFile("RR\t" + Quantum + "\n");

        //sort processList By ArrivalTime
        sortProcessListByArriveTime();

        // تا زمانی که لیست پروسس ها یا صف پردازه ها خالی نشده است ادامه بده
        while (!processesList.isEmpty() || !readyQueue.isEmpty()){

            // تا زمانی که زمان ورود پروسس اول لیست فرا رسیده
            // آنرا به صف اضافه کن
            if(!processesList.isEmpty()){
                while (processesList.get(0).arrivalTime<=TIME){
                    process t = processesList.remove(0);
                    t.waitingTime = TIME; // set waiting time Start
                    readyQueue.add(t);
                    if(processesList.isEmpty()) break;
                }
            }

            if(readyQueue.isEmpty()){
                TIME += 1;
                continue;
            }

             /*
            در این روش اگر زمان اجرای پروسس کمتر از کوانتوم باشد مثل FIFO
            انجام شده و تمام میشود. ولی اگر بزرگتر از ان باشد زمان انرا منهای کوانتوم میکنیم و
            دوباره به اخر صف اضافه میکنیم آن پروسس را تا دوباره انجام بشود
             */
            process temp = readyQueue.remove(0);

            if (temp.executeTime <= Quantum){
                writeToFile(TIME + "  " + temp.id+"\n");

                if(temp.waitingTime<0) temp.waitingTime *= -1;
                temp.waitingTime = TIME - temp.waitingTime; // set waiting time

                TIME += (long)temp.executeTime;
                doneProcessesList.add(temp);

            }
            else { // temp.executeTime > preemptiveQuantum
                writeToFile(TIME + "  " + temp.id+"\n");
                TIME += (long)Quantum;
                temp.executeTime -= Quantum;
                temp.waitingTime -= Quantum;
                // agar ghable tamam shodan process ghabli, jadid biyayad
                if(!processesList.isEmpty()){
                    while (processesList.get(0).arrivalTime<=TIME){
                        process t = processesList.remove(0);
                        t.waitingTime = TIME; // set waiting time Start
                        readyQueue.add(t);
                        if(processesList.isEmpty()) break;
                    }
                }
                readyQueue.add(temp);
            }
        }
        double sum = 0;
        for (int i=0;i<countOfProcesses;i++){
            sum+= doneProcessesList.get(i).waitingTime;
        }
        writeToFile("AVG Waiting Time: " + sum/(double) countOfProcesses);
    }

    static void SJF(){

        ArrayList<process> readyQueue = new ArrayList<>();
        writeToFile("SJF\n");

        //sort processList By arrival time
        sortProcessListByArriveTime();

        // sort again with execute time if arrive time was <= TIME
        sortProcessListByExecuteTime();

        // تا زمانی که لیست پروسس ها یا صف پردازه ها خالی نشده است ادامه بده
        while (!processesList.isEmpty() || !readyQueue.isEmpty()){

            // sort again with execute time if arrive time was <= TIME
//            sortProcessListByExecuteTime();

            // تا زمانی که زمان ورود پروسس اول لیست فرا رسیده
            // آنرا به صف اضافه کن

            if(!processesList.isEmpty()){
                while (processesList.get(0).arrivalTime<=TIME){
                    process t = processesList.remove(0);
                    t.waitingTime = TIME; // set waiting time Start
                    readyQueue.add(t);
                    if(processesList.isEmpty()) break;
                }
                readyQueue = sortReadyQueueByExecuteTime(readyQueue);
            }

            if(readyQueue.isEmpty()){
                TIME += 1;
                continue;
            }

            process temp = readyQueue.remove(0);
            writeToFile(TIME + "  " + temp.id+"\n");
            temp.waitingTime = TIME - temp.waitingTime; // set waiting time
            TIME += temp.executeTime;
            doneProcessesList.add(temp);
        }

        double sum = 0;
        for (int i=0;i<countOfProcesses;i++){
            sum+= doneProcessesList.get(i).waitingTime;
        }
        writeToFile("AVG Waiting Time: " + sum/(double) countOfProcesses);

    }

    // غیر انحصاری- بدون قطع کردن از وسط
    static void priorityWithPremp(){
        ArrayList<process> readyQueue = new ArrayList<>();
        writeToFile("PR_WithPREMP\n");
        //sort processList By ArrivalTime
        sortProcessListByArriveTime();
        process tempBefore = null;
        boolean flag = false;
        // تا زمانی که لیست پروسس ها یا صف پردازه ها خالی نشده است ادامه بده
        while (!processesList.isEmpty() || !readyQueue.isEmpty()){

            // تا زمانی که سایز صف کمتر مساوی 100 است و زمان ورود پروسس اول لیست فرا رسیده
            // آنرا به صف اضافه کن
            if(!processesList.isEmpty()){
                while (processesList.get(0).arrivalTime<=TIME){
                    process t = processesList.remove(0);
                    t.waitingTime = TIME; // set waiting time Start
                    readyQueue.add(t);
                    if(processesList.isEmpty()) break;
                }
                //sort readyQueue by priority of processes
                readyQueue = sortReadyQueueByPriority(readyQueue);
            }
            if(!readyQueue.isEmpty() && !flag) {
                writeToFile(TIME + "  " + readyQueue.get(0).id + "\n");
                flag = true;
            }
            if(readyQueue.isEmpty()){
                TIME += 1;
                continue;
            }

            process temp = readyQueue.get(0); // just peak process, not remove from queue
            if(tempBefore != null && tempBefore !=temp )
                 writeToFile(TIME + "  " + temp.id+"\n");

            TIME+=1;
            temp.executeTime--;
            if(temp.executeTime==0){
                process exTime = getProcessByID(temp.id);
                temp.waitingTime = TIME - temp.waitingTime - exTime.executeTime; // set waiting time
                doneProcessesList.add(temp);
                readyQueue.remove(0);
                if(!readyQueue.isEmpty())
                     writeToFile(TIME + "  " + readyQueue.get(0).id+"\n");
            }
            if(!readyQueue.isEmpty())
                tempBefore=  readyQueue.get(0);

        }
        double sum = 0;
        for (int i=0;i<countOfProcesses;i++){
            sum+= doneProcessesList.get(i).waitingTime;
        }
        writeToFile("AVG Waiting Time: " + sum/(double) countOfProcesses);

    }

    // نحصاری- با قطع کردن از وسط
    static void priorityWithNoPremp(){
        ArrayList<process> readyQueue = new ArrayList<>();
        writeToFile("PR_noPREMP\n");
        //sort processList By ArrivalTime
        sortProcessListByArriveTime();

        // تا زمانی که لیست پروسس ها یا صف پردازه ها خالی نشده است ادامه بده
        while (!processesList.isEmpty() || !readyQueue.isEmpty()){

            //  زمان ورود پروسس اول لیست فرا رسیده
            // آنرا به صف اضافه کن
            if(!processesList.isEmpty()){
                while (processesList.get(0).arrivalTime<=TIME){
                    process t = processesList.remove(0);
                    t.waitingTime = TIME; // set waiting time Start
                    readyQueue.add(t);
                    if(processesList.isEmpty()) break;
                }
                //sort readyQueue by priority of processes
                readyQueue = sortReadyQueueByPriority(readyQueue);
            }

            if(readyQueue.isEmpty()){
                TIME += 1;
                continue;
            }

            writeToFile(TIME + "  " + readyQueue.get(0).id + "\n");
            process temp = readyQueue.remove(0);
            temp.waitingTime = TIME - temp.waitingTime; // set waiting time
            TIME += (long)temp.executeTime;
            doneProcessesList.add(temp);
        }
        double sum = 0;
        for (int i=0;i<countOfProcesses;i++){
            sum+= doneProcessesList.get(i).waitingTime;
        }
        writeToFile("AVG Waiting Time: " + sum/(double) countOfProcesses);

    }

    static void sortProcessListByArriveTime(){
        //sort processList By ArrivalTime
        processesList.sort(new Comparator<process>() {
            @Override
            public int compare(process o1, process o2) {
                if(o1.arrivalTime>o2.arrivalTime)return 1;
                else if(o1.arrivalTime==o2.arrivalTime)return 0;
                else return -1;
            }
        });
    }

    static void sortProcessListByExecuteTime(){
        // sort again with execute time if arrive time was <= TIME
        processesList.sort(new Comparator<process>() {
            @Override
            public int compare(process o1, process o2) {
                if(o1.arrivalTime<=TIME && o2.arrivalTime<=TIME) {
                    if (o1.executeTime > o2.executeTime) return 1;
                    else if (o1.executeTime == o2.executeTime) return 0;
                    else return -1;
                }else
                    return 1;
            }
        });
    }

    static ArrayList<process> sortReadyQueueByExecuteTime(ArrayList<process> template){
        // sort again with execute time if arrive time was <= TIME
        template.sort(new Comparator<process>() {
            @Override
            public int compare(process o1, process o2) {
                if (o1.executeTime > o2.executeTime) return 1;
                else if (o1.executeTime == o2.executeTime) return 0;
                else return -1;
            }
        });
        return template;
    }

    static ArrayList<process> sortReadyQueueByPriority(ArrayList<process> template){
        // sort again with execute time if arrive time was <= TIME
        template.sort(new Comparator<process>() {
            @Override
            public int compare(process o1, process o2) {
                if (o1.priority > o2.priority) return 1;
                else if (o1.priority == o2.priority) return 0;
                else return -1;
            }
        });
        return template;
    }

    static void writeToFile(String s){
        try {
            FileWriter fileWriter = new FileWriter(outputFileAddress,true);
            fileWriter.write(s);
            fileWriter.close();
        }
        catch (Exception e){
            System.out.println("File not found");
        }

    }

    static process getProcessByID (int id){
        for(int i=0;i<processesListTemp.size();i++){
            if (processesListTemp.get(i).id == id)
                return processesListTemp.get(i);
        }
        return null;
    }


    static void getProcesses (){

        try {
            File myObj = new File(inputFileAddress);
            Scanner myReader = new Scanner(myObj);
            myReader.nextLine(); // first line
             countOfProcesses = myReader.nextInt();
            for(int i=0;i<countOfProcesses;i++){
                int id,arr,cb,ps;
                id = myReader.nextInt();
                arr = myReader.nextInt();
                cb = myReader.nextInt();
                ps = myReader.nextInt();
                processesList.add(new process(id,arr,cb,ps));
                processesListTemp.add(new process(id,arr,cb,ps));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

class process {

    int id;
    int arrivalTime;
    int executeTime;
    int priority;
    long waitingTime;

    public process(int id,int arrivalTime,int cpuBurst, int priority){
        this.id=id;
        this.arrivalTime = arrivalTime;
        this.executeTime =cpuBurst;
        this.priority = priority;
        waitingTime =0;
    }
}
