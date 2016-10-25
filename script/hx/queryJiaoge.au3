Global $main = WinActivate("网上股票交易系统5.0")
;ControlClick($main, "", "[CLASS:SysTreeView32; INSTANCE:1]", "left", 1, 77, 315)

queryJiaoge($CmdLine[1], $CmdLine[2])

Func queryJiaoge($startDate, $endDate)
	$main = WinActivate("网上股票交易系统5.0")
   ;ControlClick($main, "", "[CLASS:SysTreeView32; INSTANCE:1]", "left", 1, 77, 315)
   ControlClick($main, "", "[CLASS:SysTreeView32; INSTANCE:1]", "left", 1, 77, 245)
   Sleep(500)
   Dim $start = ControlGetHandle($main, "", "[CLASS:SysDateTimePick32; INSTANCE:2]")
   setDate($start, $startDate)
   Dim $end = ControlGetHandle($main, "", "[CLASS:SysDateTimePick32; INSTANCE:3]")
   setDate($end, $endDate)
   ControlClick($main, "", "确定")

   Sleep(3000)
   ;[AfxMDIFrame42s][Afx:400000:b:10003:6:15ec1b59][#32769]
   ;[AfxMDIFrame42s][Afx:400000:b:10003:6:1ab81107][#32769]
   ;ControlClick($main, "", "[CLASS:CVirtualGridCtrl; INSTANCE:3]")
   ControlClick($main, "", "[CLASS:CVirtualGridCtrl; INSTANCE:2]")
   Sleep(1000)
   Send("^c")
   Sleep(1000)
   Dim $text = ClipGet()
   ;MsgBox(0,"", $text);
   ConsoleWrite($text)
EndFunc

Func setDate($ctrl, $date)

	;ControlSetText ($main, "", $ctrl, "2016-09-07")
	;MsgBox(0,"",ControlGetText($main,"",$ctrl));
   Dim $time = StringSplit($date, "-")
   ControlClick($main, "", $ctrl, "left", 1, 20, 12)
   Send($time[1])
   ;MsgBox(0,"",$time[1]);
   Sleep(50)
   ControlClick($main, "", $ctrl, "left", 1, 35, 12)
   Send($time[2])
   ; MsgBox(0,"",$time[2]);
   Sleep(50)
   ControlClick($main, "", $ctrl, "left", 1, 50, 12)
   Send($time[3])
   ; MsgBox(0,"",$time[3]);
   Sleep(50)
EndFunc