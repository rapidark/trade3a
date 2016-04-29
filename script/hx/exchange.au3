Global $main = WinActivate("网上股票交易系统5.0")
exchange($CmdLine[1], $CmdLine[2], $CmdLine[3], $CmdLine[4], $CmdLine[5], $CmdLine[6])

Func exchange($buyCode, $buyPrice, $buyNum, $sellCode, $sellPrice, $sellNum)
   Send("{F6}")
   Sleep(100)
   ControlSetText($main, "", "[CLASS:Edit;INSTANCE:1]", $buyCode)
   Sleep(1000)
   ControlSetText($main, "", "[CLASS:Edit;INSTANCE:2]", $buyPrice)
   ControlSetText($main, "", "[CLASS:Edit;INSTANCE:3]", $buyNum)
   Sleep(1000)
   ControlSetText($main, "", "[CLASS:Edit; INSTANCE:4]", $sellCode)
   Sleep(1000)
   ControlSetText($main, "", "[CLASS:Edit; INSTANCE:5]", $sellPrice)
   ControlSetText($main, "", "[CLASS:Edit; INSTANCE:6]", $sellNum)
   Sleep(1000)
   ControlClick($main, "", "同时买卖")

   Dim $confirm = WinWait("", "委托确认");
   ControlClick($confirm, "", "是(&Y)")
   Dim $tip = WinWait("", "提示");
   Dim $text = WinGetText($tip)
   ControlClick($tip, "", "确定")
   return $text
EndFunc