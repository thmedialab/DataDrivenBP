(* ::Package:: *)

CharacterEncoding->"UTF-8"


oxiAmpMatrix=Import[FileNameJoin[{NotebookDirectory[],"oxiAmp2.dat"}],"Table"];


colLenth=Map[Length[oxiAmpMatrix[[#]]]&,Range[1,oxiAmpMatrix//Length]]


timeStampGenerator[l_]:=Range[0,50 (l-1),50];
timeLists=timeStampGenerator /@ colLenth;


Length[timeLists[[1]]]


Length[oxiAmpMatrix]


onePair[i_,timeLists_,oxiAmpMatrix_]:=Table[{timeLists[[i]][[j]],oxiAmpMatrix[[i]][[j]]},{j,1,Length[timeLists[[i]]]}];


onePair[1,timeLists,oxiAmpMatrix]


Range[1,Length[oxiAmpMatrix]]


Clear[pairs]
pairs[oxiAmpMatrix_,timeLists_]:= Map[onePair[#,timeLists,oxiAmpMatrix]&,Range[1,Length[oxiAmpMatrix]]];


(*peakValley[data_]:=ReplaceList[data, ({___, {_, a_}, {t_, b_}, {_, c_}, ___} /; 
    Or[a < b > c, a > b < c]) :> {t, b}]
peakValley[pairs[[1]]]*)


pairs[oxiAmpMatrix,timeLists]


wt[x_]:=ContinuousWaveletTransform[x,MexicanHatWavelet[1]];


cwt=wt[oxiAmpMatrix[[1]]]


cwt["Scales"]


cwtCorrespondingScaleValues=cwt[{3,4},"Values"];



ListLinePlot[cwtCorrespondingScaleValues[[1]][[1;;200]]]


(* ::InheritFromParent:: *)
(**)


(*peakValley[data_]:=ReplaceList[data, ({___, {_, a_}, {t_, b_}, {_, c_}, ___} /; 
    Or[a < b > c, a > b < c]) :> {t, b}]*)


(*Table[{timeLists[[1]][[i]],cwtCorrespondingScaleValues[[1]][[i]]},{i,1,Length[timeLists[[1]]]}]*)
joinTwoList[x_List,y_List]:=Table[{x[[i]],y[[i]]},{i,1,Length[x]}]/;Length[x]==Length[y];
cwtPair=joinTwoList[timeLists[[1]],cwtCorrespondingScaleValues[[1]]];

mins=Pick[cwtPair,MinDetect[cwtPair[[All,2]]],1];
maxs=Pick[cwtPair,MaxDetect[cwtPair[[All,2]]],1];



beatUnit[mins_,maxs_]:=Module[{firstValley,firstPeak,secondValley},
	firstValley=Cases[mins,{x_,y_}/;y<0][[1]];
	firstPeak=Cases[maxs,{x_,y_}/;(y>0 && x>firstValley[[1]])][[1]];
	secondValley=Cases[mins,{x_,y_}/;(y<0 && x>firstPeak[[1]]&& Abs[y]>firstPeak[[2]]/2)][[1]];
	Return[{firstValley,firstPeak,secondValley}];
];


(*lastUnit=beatUnit[mins,maxs]
lastUnit[[3]][[1]]*)
listUnits[minsO_,maxsO_]:=Module[{unitList,lastUnit,mins,maxs},
	mins=minsO;maxs=maxsO;
	unitList={};
	Do[
	Clear[lastUnit];
	lastUnit=beatUnit[mins,maxs];
	unitList=Append[unitList,lastUnit];
	mins=DeleteCases[mins,{x_,y_}/;x<lastUnit[[3]][[1]]];
	maxs=DeleteCases[maxs,{x_,y_}/;x<lastUnit[[3]][[1]]];
	,{100}];
	unitList[[Range[Length[unitList]-49,Length[unitList]]]]
];
unitList=listUnits[mins,maxs];


Length[unitList]


lastPartUnits=unitList;


Manipulate[Show[ListLinePlot[cwtPair[[Range[high-200,high]]]], 
     ListPlot[Flatten[lastPartUnits,1], PlotStyle -> Red]], 
     {{high,201,"High"},201,Length[cwtPair]}]


averageT1[unitList_]:=
Module[{sum,t1List,average},
sum=0;
t1List=Map[Function[{unit},unit[[2]][[1]]-unit[[1]][[1]]],unitList];
average=N[Mean[t1List]]
];


averageT1[lastPartUnits]


averageT2[unitList_]:=
Module[{sum,t1List,average},
sum=0;
t1List=Map[Function[{unit},unit[[3]][[1]]-unit[[2]][[1]]],unitList];
average=N[Mean[t1List]]
];


averageT2[lastPartUnits]


heartRate[aveT1_,aveT2_]:=Round[60000/(aveT1+aveT2)];


heartRate[averageT2[lastPartUnits],averageT1[lastPartUnits]]


cwtValues[cwt_]:=Re[cwt[{3,4},"Values"]];


feature[]:=
Module[{oxiAmpMatrix,bpList,systolicList,diastolicList,ageList,genderList,colLenth,timeLists,pairList,cwtList, cwtValueList,cwtValueListFlat,cwtPairList,minList,maxList,unitsLists,averageT1List,averageT2List,hrList,sysFeatureList,diasFeatureList},
oxiAmpMatrix=Import[FileNameJoin[{NotebookDirectory[],"oxi.dat"}],"Table"];
bpList=Import[FileNameJoin[{NotebookDirectory[],"bp2.dat"}],"Table"];
systolicList=Map[#[[1]]&,bpList];
diastolicList=Map[#[[2]]&,bpList];
ageList=Map[#[[4]]&,bpList];
genderList=Map[#[[5]]&,bpList];
colLenth=Map[Length[oxiAmpMatrix[[#]]]&,Range[1,oxiAmpMatrix//Length]];
timeLists=timeStampGenerator /@ colLenth;
pairList=pairs[oxiAmpMatrix,timeLists];
cwtList=Map[wt,oxiAmpMatrix];
cwtValueList=Map[cwtValues,cwtList];
cwtValueListFlat=Flatten[cwtValueList,1];
cwtPairList = pairs[cwtValueListFlat,timeLists];
minList=Map[mins,cwtPairList];
maxList=Map[maxs,cwtPairList];
unitsLists=Map[listUnits[minList[[#]],maxList[[#]]]&,Range[1,Length[minList]]];
averageT1List=Map[averageT1,unitsLists];
averageT2List=Map[averageT2,unitsLists];
hrList=Map[heartRate[averageT1List[[#]],averageT2List[[#]]]&,Range[1,Length[averageT1List]]];
sysFeatureList=MapThread[List,{systolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
diasFeatureList=MapThread[List,{diastolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
Export[FileNameJoin[{NotebookDirectory[],"sysFeature.dat"}],sysFeatureList,"Table"];
Export[FileNameJoin[{NotebookDirectory[],"diasFeature.dat"}],diasFeatureList,"Table"];
]


Clear[mins,maxs]
mins[pair_]:=Pick[pair,MinDetect[pair[[All,2]]],1];
maxs[pair_]:=Pick[pair,MaxDetect[pair[[All,2]]],1];





featureScaled[]:=
Module[{oxiAmpMatrix,bpList,systolicList,diastolicList,ageList,genderList,colLenth,timeLists,pairList,cwtList, cwtValueList,cwtValueListFlat,cwtPairList,minList,maxList,unitsLists,averageT1List,averageT2List,hrList,sysFeatureList,diasFeatureList},
oxiAmpMatrix=Import[FileNameJoin[{NotebookDirectory[],"oxi.dat"}],"Table"];
bpList=Import[FileNameJoin[{NotebookDirectory[],"bp2.dat"}],"Table"];
systolicList=Map[#[[1]]&,bpList];
diastolicList=Map[#[[2]]&,bpList];
ageList=Map[#[[4]]&,bpList];
genderList=Map[#[[5]]&,bpList];
colLenth=Map[Length[oxiAmpMatrix[[#]]]&,Range[1,oxiAmpMatrix//Length]];
timeLists=timeStampGenerator /@ colLenth;
pairList=pairs[oxiAmpMatrix,timeLists];
cwtList=Map[wt,oxiAmpMatrix];
cwtValueList=Map[cwtValues,cwtList];
cwtValueListFlat=Flatten[cwtValueList,1];
cwtPairList = pairs[cwtValueListFlat,timeLists];
minList=Map[mins,cwtPairList];
maxList=Map[maxs,cwtPairList];
unitsLists=Map[listUnits[minList[[#]],maxList[[#]]]&,Range[1,Length[minList]]];
averageT1List=Map[averageT1,unitsLists];
averageT2List=Map[averageT2,unitsLists];
hrList=Map[heartRate[averageT1List[[#]],averageT2List[[#]]]&,Range[1,Length[averageT1List]]];
averageT1List=N[Standardize[averageT1List]];
averageT2List=N[Standardize[averageT2List]];
hrList=N[Standardize[hrList]];
ageList=N[Standardize[ageList]];
genderList=N[Standardize[genderList]];
sysFeatureList=MapThread[List,{systolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
diasFeatureList=MapThread[List,{diastolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
Export[FileNameJoin[{NotebookDirectory[],"sysFeatureStandardized.dat"}],sysFeatureList,"Table"];
Export[FileNameJoin[{NotebookDirectory[],"diasFeatureStandardized.dat"}],diasFeatureList,"Table"];
]


featureNormalized[]:=
Module[{oxiAmpMatrix,bpList,systolicList,diastolicList,ageList,genderList,colLenth,timeLists,pairList,cwtList, cwtValueList,cwtValueListFlat,cwtPairList,minList,maxList,unitsLists,averageT1List,averageT2List,hrList,sysFeatureList,diasFeatureList},
oxiAmpMatrix=Import[FileNameJoin[{NotebookDirectory[],"oxi.dat"}],"Table"];
bpList=Import[FileNameJoin[{NotebookDirectory[],"bp2.dat"}],"Table"];
systolicList=Map[#[[1]]&,bpList];
diastolicList=Map[#[[2]]&,bpList];
ageList=Map[#[[4]]&,bpList];
genderList=Map[#[[5]]&,bpList];
colLenth=Map[Length[oxiAmpMatrix[[#]]]&,Range[1,oxiAmpMatrix//Length]];
timeLists=timeStampGenerator /@ colLenth;
pairList=pairs[oxiAmpMatrix,timeLists];
cwtList=Map[wt,oxiAmpMatrix];
cwtValueList=Map[cwtValues,cwtList];
cwtValueListFlat=Flatten[cwtValueList,1];
cwtPairList = pairs[cwtValueListFlat,timeLists];
minList=Map[mins,cwtPairList];
maxList=Map[maxs,cwtPairList];
unitsLists=Map[listUnits[minList[[#]],maxList[[#]]]&,Range[1,Length[minList]]];
averageT1List=Map[averageT1,unitsLists];
averageT2List=Map[averageT2,unitsLists];
hrList=Map[heartRate[averageT1List[[#]],averageT2List[[#]]]&,Range[1,Length[averageT1List]]];
averageT1List=N[Normalize[averageT1List]];
averageT2List=N[Normalize[averageT2List]];
hrList=N[Normalize[hrList]];
ageList=N[Normalize[ageList]];
genderList=N[Normalize[genderList]];
sysFeatureList=MapThread[List,{systolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
diasFeatureList=MapThread[List,{diastolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
Export[FileNameJoin[{NotebookDirectory[],"sysFeatureNormalized.dat"}],sysFeatureList,"Table"];
Export[FileNameJoin[{NotebookDirectory[],"diasFeatureNormalized.dat"}],diasFeatureList,"Table"];
]





featureRescaled[]:=
Module[{oxiAmpMatrix,bpList,systolicList,diastolicList,ageList,genderList,colLenth,timeLists,pairList,cwtList, cwtValueList,cwtValueListFlat,cwtPairList,minList,maxList,unitsLists,averageT1List,averageT2List,hrList,sysFeatureList,diasFeatureList},
oxiAmpMatrix=Import[FileNameJoin[{NotebookDirectory[],"oxi.dat"}],"Table"];
bpList=Import[FileNameJoin[{NotebookDirectory[],"bp2.dat"}],"Table"];
systolicList=Map[#[[1]]&,bpList];
diastolicList=Map[#[[2]]&,bpList];
ageList=Map[#[[4]]&,bpList];
genderList=Map[#[[5]]&,bpList];
colLenth=Map[Length[oxiAmpMatrix[[#]]]&,Range[1,oxiAmpMatrix//Length]];
timeLists=timeStampGenerator /@ colLenth;
pairList=pairs[oxiAmpMatrix,timeLists];
cwtList=Map[wt,oxiAmpMatrix];
cwtValueList=Map[cwtValues,cwtList];
cwtValueListFlat=Flatten[cwtValueList,1];
cwtPairList = pairs[cwtValueListFlat,timeLists];
minList=Map[mins,cwtPairList];
maxList=Map[maxs,cwtPairList];
unitsLists=Map[listUnits[minList[[#]],maxList[[#]]]&,Range[1,Length[minList]]];
averageT1List=Map[averageT1,unitsLists];
averageT2List=Map[averageT2,unitsLists];
hrList=Map[heartRate[averageT1List[[#]],averageT2List[[#]]]&,Range[1,Length[averageT1List]]];
averageT1List=N[Rescale[averageT1List]];
averageT2List=N[Rescale[averageT2List]];
hrList=N[Rescale[hrList]];
ageList=N[Rescale[ageList]];
genderList=N[Rescale[genderList]];
sysFeatureList=MapThread[List,{systolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
diasFeatureList=MapThread[List,{diastolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
Export[FileNameJoin[{NotebookDirectory[],"sysFeatureRescaled.dat"}],sysFeatureList,"Table"];
Export[FileNameJoin[{NotebookDirectory[],"diasFeatureRescaled.dat"}],diasFeatureList,"Table"];
]





camFeatureRescaled[]:=
Module[{camAmpMatrix,bpList,systolicList,diastolicList,ageList,genderList,colLenth,timeLists,pairList,cwtList, cwtValueList,cwtValueListFlat,cwtPairList,minList,maxList,unitsLists,averageT1List,averageT2List,hrList,sysFeatureList,diasFeatureList},
camAmpMatrix=Import[FileNameJoin[{NotebookDirectory[],"cam.dat"}],"Table"];
bpList=Import[FileNameJoin[{NotebookDirectory[],"bp2.dat"}],"Table"];
systolicList=Map[#[[1]]&,bpList];
diastolicList=Map[#[[2]]&,bpList];
ageList=Map[#[[4]]&,bpList];
genderList=Map[#[[5]]&,bpList];
colLenth=Map[Length[camAmpMatrix[[#]]]&,Range[1,camAmpMatrix//Length]];
timeLists=timeStampGenerator /@ colLenth;
pairList=pairs[camAmpMatrix,timeLists];
cwtList=Map[wt,camAmpMatrix];
cwtValueList=Map[cwtValues,cwtList];
cwtValueListFlat=Flatten[cwtValueList,1];
cwtPairList = pairs[cwtValueListFlat,timeLists];
minList=Map[mins,cwtPairList];
maxList=Map[maxs,cwtPairList];
unitsLists=Map[listUnits[minList[[#]],maxList[[#]]]&,Range[1,Length[minList]]];
averageT1List=Map[averageT1,unitsLists];
averageT2List=Map[averageT2,unitsLists];
hrList=Map[heartRate[averageT1List[[#]],averageT2List[[#]]]&,Range[1,Length[averageT1List]]];
averageT1List=N[Rescale[averageT1List]];
averageT2List=N[Rescale[averageT2List]];
hrList=N[Rescale[hrList]];
ageList=N[Rescale[ageList]];
genderList=N[Rescale[genderList]];
sysFeatureList=MapThread[List,{systolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
diasFeatureList=MapThread[List,{diastolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
Export[FileNameJoin[{NotebookDirectory[],"camSysFeatureRescaled.dat"}],sysFeatureList,"Table"];
Export[FileNameJoin[{NotebookDirectory[],"camDiasFeatureRescaled.dat"}],diasFeatureList,"Table"];
]


camAmpMatrix=Import[FileNameJoin[{NotebookDirectory[],"cam.dat"}],"Table"];
bpList=Import[FileNameJoin[{NotebookDirectory[],"bp.dat"}],"Table"];
systolicList=Map[#[[1]]&,bpList];
diastolicList=Map[#[[2]]&,bpList];
ageList=Map[#[[4]]&,bpList];
genderList=Map[#[[5]]&,bpList];




timeLists=Import[FileNameJoin[{NotebookDirectory[],"camTime.dat"}],"Table"];
pairList=pairs[camAmpMatrix,timeLists];
cwtList=Map[wt,camAmpMatrix];
cwtValueList=Map[cwtValues,cwtList];
cwtValueListFlat=Flatten[cwtValueList,1];
cwtPairList = pairs[cwtValueListFlat,timeLists];
minList=Map[mins,cwtPairList];
maxList=Map[maxs,cwtPairList];
ind=Flatten[Position[Map[Length[#]&,minList]-Map[Length[#]&,maxList],_?(Abs[#] < 5 &)]];
minList=minList[[ind]];
maxList=maxList[[ind]];



minList


(*minList=minList[[indexes]];
maxList=maxList[[indexes]];*)


beatUnitCam[mins_,maxs_]:=Module[{firstValley,firstPeak,secondValley},
	firstValley=Cases[mins,{x_,y_}/;y<0][[1]];
	firstPeak=Cases[maxs,{x_,y_}/;(y>0 && x>firstValley[[1]])][[1]];
	secondValley=Cases[mins,{x_,y_}/;(y<0 && x>firstPeak[[1]]&& Abs[y]>firstPeak[[2]]/2)][[1]];
	Return[{firstValley,firstPeak,secondValley}];
];


listUnitsCam[minsO_,maxsO_]:=Module[{unitList,lastUnit,mins,maxs},
	mins=minsO;maxs=maxsO;
	unitList={};
	Do[
	Clear[lastUnit];
	lastUnit=beatUnitCam[mins,maxs];
	unitList=Append[unitList,lastUnit];
	Print[lastUnit];
	mins=DeleteCases[mins,{x_,y_}/;x<lastUnit[[3]][[1]]];
	maxs=DeleteCases[maxs,{x_,y_}/;x<lastUnit[[3]][[1]]];
	Print["Length of unitList:" , Length[unitList]];
	Print["Length of mins:" , Length[mins]];
	Print["Length of maxs:" , Length[maxs]];
	,{50}];
	unitList[[Range[Length[unitList]-49,Length[unitList]]]]
];


unitsLists=Map[listUnitsCam[minList[[#]],maxList[[#]]]&,Range[1,Length[minList]]];



notRight={1,6,8,10,11,12,13,14,16,17,18,19,20,21,23,24,25}


listUnitsCam[minList[[26]],maxList[[26]]]


averageT1List=Map[averageT1,unitsLists];
averageT2List=Map[averageT2,unitsLists];
hrList=Map[heartRate[averageT1List[[#]],averageT2List[[#]]]&,Range[1,Length[averageT1List]]];
averageT1List=N[Rescale[averageT1List]];
averageT2List=N[Rescale[averageT2List]];
hrList=N[Rescale[hrList]];
ageList=N[Rescale[ageList]];
genderList=N[Rescale[genderList]];
sysFeatureList=MapThread[List,{systolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
diasFeatureList=MapThread[List,{diastolicList,averageT1List,averageT2List,hrList,ageList,genderList}];
Export[FileNameJoin[{NotebookDirectory[],"camSysFeatureRescaled.dat"}],sysFeatureList,"Table"];
Export[FileNameJoin[{NotebookDirectory[],"camDiasFeatureRescaled.dat"}],diasFeatureList,"Table"];


Map[Length[#]&,minList]-Map[Length[#]&,maxList]


ind=Flatten[Position[Map[Length[#]&,minList]-Map[Length[#]&,maxList],_?(Abs[#] < 5 &)]]


Length[%]


cwtPairPos=cwtPairList[[ind]]


Length[cwtPairPos]


ListLinePlot[minList[[1]]]


Map[Manipulate[Show[ListLinePlot[cwtPairPos[[#]][[Range[high-200,high]]]],ListPlot[minList,PlotStyle -> Red],ListPlot[maxList,PlotStyle -> Green]], 
     {{high,201,"High"},201,Length[cwtPairPos[[#]]]}]&,Range[40,Length[cwtPairPos]]]


indexes={2,3,4,5,7,9,10,14,16,17,18,20,21,23,25,29,30,31,32,34,35,36,40,41,42,44}


Length[%]


Map[Length,minList]
Map[Length,maxList]






