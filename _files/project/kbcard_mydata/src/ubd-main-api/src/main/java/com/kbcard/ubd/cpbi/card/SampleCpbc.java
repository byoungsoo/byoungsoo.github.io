package com.kbcard.ubd.cpbi.card;

import com.kbcard.ubd.common.service.externalinterface.adaptor.http.LinkHttpAdaptor;
import com.kbcard.ubd.constants.InterfaceConst.TargetTypeConst;

import devon.core.collection.LData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.ext.core.exception.LBizException;

/**
 * 
 *
 * @logicalName   마이데이터고객포인트잔여조회Pbi
 * @lastDate      2021-05-31
 */
public class SampleCpbc {
	
	/**
	 * 
	 *
	 * @serviceID     UBD1000340
	 * @logicalName   마이데이터포인트정보목록조회
	 * @param         LData input 
	 *                            LData iRetvLstMydtPntInfP i마이데이터포인트정보목록조회P : 포인트정보목록조회입력Dto
	 * @return        LData rData 
	 *                            LData rRetvLstMydtPntInfP r마이데이터포인트정보목록조회P : 포인트정보목록조회Dtodm
	 * @exception     LException occurs error 
	 * @modelProject  GAB_MODEL_포인트
	 * @fullPath      2.시스템명세모델::03.프로세스컴포넌트::10.포인트조회::02.포인트조회::마이데이터고객포인트잔여조회송신Pbi::CORA_마이데이터고객포인트잔여조회송신Pbi::ACSD_마이데이터포인트정보목록조회
	 * 
	 */
	public LData retvLstMydtPntInf( LData input ) throws LException {
		LLog.debug.println( input );
		LLog.debug.println( " ☆☆☆ 마이데이터포인트정보목록조회 pbi ☆☆☆ START " );
		
		LData iRetvLstMydtPntInfP = new LData();
		LData rRetvLstMydtPntInfP = new LData();
		
		LLog.debug.println( rRetvLstMydtPntInfP );
		LLog.debug.println( " ☆☆☆ 마이데이터포인트정보목록조회 pbi ☆☆☆ END " );
		return rRetvLstMydtPntInfP;
	}


}



