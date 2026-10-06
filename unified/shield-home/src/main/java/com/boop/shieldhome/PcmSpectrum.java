package com.boop.shieldhome;
/** Windowed stereo PCM power spectrum. Fixed dBFS scale, no generated motion or auto gain. */
final class PcmSpectrum {
 static final int BANDS=20,N=4096,HOP=1024;
 private final int rate;private final double[][] ring=new double[2][N];private final double[] re=new double[N],im=new double[N],power=new double[N/2+1],window=new double[N];private int cursor,filled,hop;private float[] latest=new float[BANDS];
 PcmSpectrum(int rate){this.rate=rate;for(int i=0;i<N;i++)window[i]=0.5-0.5*Math.cos(2*Math.PI*i/(N-1));}
 static double edge(int band){return 40*Math.pow(400,band/(double)BANDS);}
 float[] feed(short[] pcm,int count){for(int i=0;i+1<count;i+=2){ring[0][cursor]=pcm[i]/32768.0;ring[1][cursor]=pcm[i+1]/32768.0;cursor=(cursor+1)%N;filled=Math.min(N,filled+1);if(++hop>=HOP&&filled==N){hop=0;analyse();}}return latest.clone();}
 private void analyse(){java.util.Arrays.fill(power,0);for(int c=0;c<2;c++){for(int i=0;i<N;i++){re[i]=ring[c][(cursor+i)%N]*window[i];im[i]=0;}fft();for(int k=1;k<=N/2;k++)power[k]+=(re[k]*re[k]+im[k]*im[k])*0.5;}
  float[] bands=new float[BANDS];for(int b=0;b<BANDS;b++){int low=Math.max(1,(int)Math.ceil(edge(b)*N/rate)),high=Math.min(N/2,(int)Math.ceil(edge(b+1)*N/rate)-1);double sum=0;for(int k=low;k<=high;k++)sum+=power[k];double rms=Math.sqrt(sum*2/(N*(double)N*0.375));double db=rms>0?20*Math.log10(rms):-120;bands[b]=(float)Math.max(0,Math.min(1,(db+72)/72));}latest=bands;
 }
 private void fft(){for(int i=1,j=0;i<N;i++){int bit=N>>1;for(; (j&bit)!=0;bit>>=1)j^=bit;j^=bit;if(i<j){double t=re[i];re[i]=re[j];re[j]=t;}}
  for(int len=2;len<=N;len<<=1){double angle=-2*Math.PI/len,cr=Math.cos(angle),ci=Math.sin(angle);for(int start=0;start<N;start+=len){double wr=1,wi=0;for(int j=0;j<len/2;j++){int a=start+j,b=a+len/2;double tr=wr*re[b]-wi*im[b],ti=wr*im[b]+wi*re[b];re[b]=re[a]-tr;im[b]=im[a]-ti;re[a]+=tr;im[a]+=ti;double next=wr*cr-wi*ci;wi=wr*ci+wi*cr;wr=next;}}}
 }
}
