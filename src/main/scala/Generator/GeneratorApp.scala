//ajndkajsndkjanskdjn
package Generator

import chisel3._
import ALU.Alu

  object GeneratorApp extends App
  {
    chisel3.Driver.execute(args, () => new Alu())
  }

