package ru.mih.day3;

import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class DiagnosticReport {

        int[] onesCount;
        int lineCount;

        public static DiagnosticReport START = new DiagnosticReport(new int[0], 0);

        public DiagnosticReport(int[] ones, int lineCount) {
            this.onesCount = ones;
            this.lineCount = lineCount;
        }

        public CompletionStage<DiagnosticReport> scan(String line){

            String onlyNumbersLine = line.trim(); //.replaceAll("[^01]", ""); odd symbols only whitespaces
            int lineLength = onlyNumbersLine.length();

            int[] newOnesCount;

            if (lineCount == 0){
                newOnesCount = new int[lineLength];
            }else {
                if (onesCount.length != lineLength){
                    throw new IllegalStateException("Wrong number in line: " + lineCount+1
                            + " we have onesCount dimension : " + onesCount.length + " lineLength: " + lineLength);
                }
                newOnesCount = Arrays.copyOf(onesCount, onesCount.length);
            }

            for(int j = 0; j<onlyNumbersLine.length();j++){
                if ('1' == onlyNumbersLine.charAt(j)) newOnesCount[j] += 1 ;
            }

            return CompletableFuture.supplyAsync(() -> new DiagnosticReport(newOnesCount, lineCount + 1));
        }

        @Override
        public String toString() {
            return String.format("GR(B): %s, GR(D): %d, ER(B): %s, ER(D): %d, PS(D): %d",
                    getGammaRateBinary(), getGammaRate(),
                    getEpsilonRateBinary(), getEpsilonRate(),
                    getGammaRate()*getEpsilonRate());

        }

        public int getPowerConsumption(){
            return getGammaRate()*getEpsilonRate();
        }

        public int getGammaRate() {
            return Integer.parseInt(getGammaRateBinary(), 2);
        }

        public int getEpsilonRate() {
            return Integer.parseInt(getEpsilonRateBinary(), 2);
        }

        public String getGammaRateBinary() {
            StringBuffer gammaRateBinary = new StringBuffer();
            for(int j = 0; j<this.onesCount.length; j++){
                if (this.onesCount[j] > this.lineCount - this.onesCount[j]){ //1s > 0s
                    gammaRateBinary.append("1");
                }else if (this.onesCount[j] == this.lineCount - this.onesCount[j]){
                    throw new IllegalStateException("1s == 0s (" + this.onesCount[j] + ") in index: " + j) ;
                }else {
                    gammaRateBinary.append("0");
                }
            }
            return gammaRateBinary.toString();
        }

        public String getEpsilonRateBinary() {

            return getGammaRateBinary()
                    .replaceAll("1", "X")
                    .replaceAll("0", "1")
                    .replaceAll("X", "0");

        }



    public static Sink<ByteString, CompletionStage<DiagnosticReport>> foundConditions(){
        return Flow.of(ByteString.class)
                .filterNot(ByteString::isEmpty)
                .map(bs -> bs.utf8String())
                .foldAsync(DiagnosticReport.START, (acc, element) -> acc.scan(element))
                .toMat(Sink.head(), Keep.right());
    }


}
