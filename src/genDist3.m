clear
clc
close all

% Generate Processing Element Distribution
N = 200; % resource count
type = 5;
CPUlambda = 7;
RAMlambda = 7;
HDDlambda = 7;
OSlambda  = 7;
APPlambda  = 7;

CPU = poissrnd(CPUlambda,N,1)+1;
RAM = poissrnd(RAMlambda,N,1)+20;
HDD = poissrnd(HDDlambda,N,1)+40;
OS =  poissrnd(OSlambda, N,1)+60;
APP = poissrnd(OSlambda, N,1)+80;

resource = [CPU; RAM; HDD; OS; APP];
src = randperm(N*type);
resource = resource(src);
xlswrite('resource.xls',resource);
dlmwrite('resource.txt', resource', 'delimiter', ',')

% Generate Job Distribution
M = 250; % Job count
CPU = poissrnd(CPUlambda,M/type,1)+1;
RAM = poissrnd(RAMlambda,M/type,1)+20;
HDD = poissrnd(HDDlambda,M/type,1)+40;
OS =  poissrnd(OSlambda,M/type,1)+60;
APP =  poissrnd(OSlambda,M/type,1)+80;
requieredResource = [CPU; RAM; HDD; OS; APP];
src = randperm(M);
requieredResource = requieredResource(src);
jobFileSize = exprnd(3500,M,1);
xlswrite('job.xls', [requieredResource round(jobFileSize)]);
dlmwrite('job.txt', [requieredResource'; round(jobFileSize)'], 'delimiter', ',')
