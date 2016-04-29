login($CmdLine[1], $CmdLine[2], $CmdLine[3])

Func login($path, $pass, $ver)
   Run($path);

   Dim $login = WinWait("用户登录")
   ControlSetText($login, "", "[CLASS:Edit;INSTANCE:2]", $pass)
   ControlSetText($login, "", "[CLASS:Edit;INSTANCE:3]", $ver)
   Sleep(500)
   ControlClick($login, "", "确定(&Y)")

   $main = WinWait("网上股票交易系统5.0")
   ;WinSetTitle($main, "", "Work")
   ;WinSetTrans($main, "", 200)

   Sleep(2000)
   Send("{F6}")
   Sleep(1000)
   ControlClick($main, "", "[CLASS:SysTreeView32; INSTANCE:1]", "left", 1, 77, 315)
   Sleep(1000)
   ControlClick($main, "", "[CLASS:SysTreeView32; INSTANCE:1]", "left", 1, 77, 295)
EndFunc
