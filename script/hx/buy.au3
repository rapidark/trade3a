Global $main = WinActivate("网上股票交易系统5.0")
buy($CmdLine[1], $CmdLine[2], $CmdLine[3])

Func buy($code, $price, $num)
   Send("{F6}")
   Sleep(100)
   ControlSetText($main, "", "[CLASS:Edit;INSTANCE:1]", $code)
   Sleep(1000)
   ControlSetText($main, "", "[CLASS:Edit;INSTANCE:2]", $price)
   ControlSetText($main, "", "[CLASS:Edit;INSTANCE:3]", $num)
   Sleep(1000)
   ControlClick($main, "", "买入[B]")

   Dim $confirm = WinWait("", "委托确认");
   ControlClick($confirm, "", "是(&Y)")
   Dim $tip = WinWait("", "提示");
   Dim $text = WinGetText($tip)
   ControlClick($tip, "", "确定")
   return $text
EndFunc
