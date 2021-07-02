package com.kbcard.ubd.cpbi.phs;

import devon.core.collection.LData;
import devon.core.collection.LMultiData;
import devon.core.exception.LException;
import devon.core.log.LLog;
import devonenterprise.business.bm.command.BizCommand;
import devonframework.business.transaction.nested.LNestedTransactionManager;

/**
 * 프로그램명 	: 마이데이터 요청제공검증내역 관리
 * 처리사항 	: 
 * 작성자 		: 임용택
 * 작성일자 	: 2021-06-03
 * 설      명 	: 마이데이터 요청제공검증내역 관리(포탈 검증용)
 */
public class DmdOferVlnHisCpbc {
	
	// 요청제공검증내역 관리
	public Boolean regtMydtApiDmdOferVlnHis(LData iDmdOferVlnHis) throws LException {

		Boolean sRtn = false;
		
		sRtn = regtMydtApiDmdVlnHis(iDmdOferVlnHis);	// 마이데이터API요청검증내역 등록
		
		sRtn = regtMydtApiOferVlnHis(iDmdOferVlnHis);	// 마이데이터API제공검증내역 등록
		
		sRtn = regtMydtApiOferVlnHisDtl(iDmdOferVlnHis);	// 마이데이터API제공검증내역상세 등록
		
		return sRtn;
		
	}
	
	/** 
	 * 마이데이터API요청검증내역 등록
	 * 1. 마이데이터요청검증내역 조회
	 * 2. 마이데이터요청검증내역 등록
	 * @param iDmdOferVlnHis
	 * @return Boolean = true/false
	 * @throws LException
	 */
	public Boolean regtMydtApiDmdVlnHis(LData iDmdOferVlnHis) throws LException {
		Boolean bRtn = false;
		int iRtn = 0;
		int iRtnHis = 0;

//		// 1. 마이데이터요청검증내역 삭제
//		iRtn = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "deleteMydtDmdVlnHis", iDmdOferVlnHis);
		
		// 1. 마이데이터요청검증내역 조회
		//    중복체크 로직 추가
		
    	// 01. transaction 분리를 위한 transaction manager 생성
        LNestedTransactionManager ntxManager = new LNestedTransactionManager();  
        try {
        	//02. transaction 분리 시작
            ntxManager.nestedBegin(); 
            
			LMultiData 	OData = new LMultiData();
			OData = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "selectMydtDmdVlnHis", iDmdOferVlnHis);
			
			if(OData.getDataCount() == 0) {
				// 2. 마이데이터요청검증내역 등록
				iRtnHis = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "insertMydtDmdVlnHis", iDmdOferVlnHis); 
			}    		
	        //03. commit 수행
	        ntxManager.nestedCommit(); //
	        
	    } catch ( Throwable t ) {
	        t.printStackTrace(LLog.err);
	        //04. 오류 발생시 rollback 수행 
	        ntxManager.nestedRollback();
	        if ( t instanceof LException ) {
	            throw (LException)t;
	        } else {
	            
	        }            
	    } finally {
	    	//05. transaction manager 종료
	    	ntxManager.nestedRelease();
	    }
	    
		return true;
	}

	/** 
	 * 마이데이터API제공검증내역 등록
	 * 1. 마이데이터제공검증내역 조회
	 * 2. 마이데이터제공검증내역 등록
	 * @param iDmdOferVlnHis
	 * @return Boolean = true/false
	 * @throws LException
	 */
	public Boolean regtMydtApiOferVlnHis(LData iDmdOferVlnHis) throws LException {
		Boolean bRtn = false;
		int iRtn = 0;
		int iRtnHis = 0;
		
//		// 1. 마이데이터제공검증내역 삭제
//		iRtn = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "deleteMydtOferVlnHis", iDmdOferVlnHis);
		
		// 1. 마이데이터제공검증내역 조회
		//   중복체크 로직 추가
		
    	// 01. transaction 분리를 위한 transaction manager 생성
        LNestedTransactionManager ntxManager = new LNestedTransactionManager();  
        try {
        	//02. transaction 분리 시작
            ntxManager.nestedBegin(); 
	            
			LMultiData 	OData = new LMultiData();
			OData = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "selectMydtOferVlnHis", iDmdOferVlnHis);
			
			if(OData.getDataCount() == 0) {		
				// 2. 마이데이터제공검증내역 등록
				iRtnHis = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "insertMydtOferVlnHis", iDmdOferVlnHis); 
			}    		
	        //03. commit 수행
	        ntxManager.nestedCommit(); //
	        
	    } catch ( Throwable t ) {
	        t.printStackTrace(LLog.err);
	        //04. 오류 발생시 rollback 수행 
	        ntxManager.nestedRollback();
	        if ( t instanceof LException ) {
	            throw (LException)t;
	        } else {
	            
	        }            
	    } finally {
	    	//05. transaction manager 종료
	    	ntxManager.nestedRelease();
	    }
	    
		return true;
	}

	/** 
	 * 마이데이터API제공검증내역상세 등록
	 * 1. 마이데이터제공검증내역상세 조회
	 * 2. 마이데이터제공검증내역상세 등록
	 * @param iDmdOferVlnHis
	 * @return Boolean = true/false
	 * @throws LException
	 */	
	public Boolean regtMydtApiOferVlnHisDtl(LData iDmdOferVlnHis) throws LException {
		Boolean bRtn = false;
		int iRtn = 0;
		int iRtnHis = 0;
		
//		// 1. 마이데이터제공검증내역상세 삭제
//		iRtn = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "deleteMydtOferVlnHisDtl", iDmdOferVlnHis);
		
		//1. 마이데이터제공검증내역상세 조회
		//   중복체크 로직 추가
		
    	// 01. transaction 분리를 위한 transaction manager 생성
        LNestedTransactionManager ntxManager = new LNestedTransactionManager();  
        try {
        	//02. transaction 분리 시작
            ntxManager.nestedBegin(); 
            
			LMultiData 	OData = new LMultiData();
			OData = (LMultiData) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "selectMydtOferVlnHisDtl", iDmdOferVlnHis);
			
			if(OData.getDataCount() == 0) {		
				// 2. 마이데이터제공검증내역상세 등록
				iRtnHis = (int) BizCommand.execute("com.kbcard.ubd.ebi.phs.DmdOferVlnHisEbc", "insertMydtOferVlnHisDtl", iDmdOferVlnHis); 
			}    		
	        //03. commit 수행
	        ntxManager.nestedCommit(); //
	        
	    } catch ( Throwable t ) {
	        t.printStackTrace(LLog.err);
	        //04. 오류 발생시 rollback 수행 
	        ntxManager.nestedRollback();
	        if ( t instanceof LException ) {
	            throw (LException)t;
	        } else {
	            
	        }            
	    } finally {
	    	//05. transaction manager 종료
	    	ntxManager.nestedRelease();
	    }
	    
		return true;
	}
 
}
