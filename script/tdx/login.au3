login($CmdLine[1], $CmdLine[2], $CmdLine[3])

Func login($path, $pass, $ver)
   Run($path);

   Dim $login = WinWait("第一创业网上行情交易系统V6.68")
   Sleep(500)
   ControlClick($login, "", "[CLASS:AfxWnd42; INSTANCE:13]")
   ControlSetText($login, "", "[CLASS:SafeEdit; INSTANCE:1]", $pass)
   ControlCommand($login, "", "[CLASS:ComboBox; INSTANCE:5]", "SetCurrentSelection", 1)
   ControlSetText($login, "", "[CLASS:SafeEdit; INSTANCE:3]", $ver)
   Sleep(500)
   ControlClick($login, "", "[CLASS:AfxWnd42; INSTANCE:3]")

   $main = WinWait("第一创业网上行情交易系统V6.68")
   ;WinSetTitle($main, "", "Work")
   ;WinSetTrans($main, "", 200)
EndFunc
