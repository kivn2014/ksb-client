	function getStatus(code){
		
		if(code=='0'){
			return "入库待分配["+code+"]";
		}else if(code=='1'){
			return "分配待处理["+code+"]"
		}else if(code=='3'){
			return "配送中["+code+"]"
		}else if(code=='5'){
			return "完成配送["+code+"]"
		}else if(code=='-1'){
			return "已经取消["+code+"]"
		}else if(code=='-2'){
			return "拒收["+code+"]"
		}
	}
	
	function getActionStatus(code){
		
		if(code=='0'){
			return "明细";
		}else if(code=='1'){
			return "取件"
		}else if(code=='3'){
			return "配送中["+code+"]"
		}else if(code=='5'){
			return "完成配送["+code+"]"
		}else if(code=='-1'){
			return "已经取消["+code+"]"
		}else if(code=='-2'){
			return "拒收["+code+"]"
		}
	}	