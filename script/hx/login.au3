login($CmdLine[1], $CmdLine[2], $CmdLine[3], $CmdLine[4])

Func login($path, $account, $pass, $ver)
   Run($path);

   Dim $login = WinWait("�û���¼")
   ControlSetText($login, "", "[CLASS:Edit;INSTANCE:1]", $account)
    ControlSetText($login, "", "[CLASS:Edit;INSTANCE:2]", $pass)
   ControlSetText($login, "", "[CLASS:Edit;INSTANCE:3]", $ver)
   Sleep(500)
   ControlClick($login, "", "ȷ��(&Y)")

   $main = WinWait("���Ϲ�Ʊ����ϵͳ5.0")
   WinSetTitle("���Ϲ�Ʊ����ϵͳ5.0", "", "AutoTraderV1")
   ;WinSetTrans($main, "", 200)

	;$main = WinActivate("���Ϲ�Ʊ����ϵͳ5.0")
	
   Sleep(2000)
   ;Send("{F6}")
   ;Sleep(1000)
   ;ControlClick($main, "", "[CLASS:SysTreeView32; INSTANCE:1]", "left", 1, 77, 315)
   ;Sleep(1000)
   ;ControlClick($main, "", "[CLASS:SysTreeView32; INSTANCE:1]", "left", 1, 77, 295)
EndFunc
