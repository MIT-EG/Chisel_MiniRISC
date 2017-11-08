//ajndkajsndkjanskdjn
package Generator

import DataStructure.ALU
import chisel3._

object GeneratorApp extends App
  {
    //chisel3.Driver.execute(args, () => new DataStructure.DataStructure())
    //chisel3.Driver.execute(args, () => new Controller.PC())
    //chisel3.Driver.execute(args, () => new Controller.Stack())

    //TODO: ezeket debugolni
    //chisel3.Driver.execute(args, () => new Controller.ProgramMemory())

    chisel3.Driver.execute(args, () => new Controller.Controller())
  }

