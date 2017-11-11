package Controller

import Chisel.Enum
import Common.{ALU_Ops, Constants, ctrl2data}
import chisel3._
import chisel3.util._
import Controller.OpCodes._

class FSM extends Chisel.Module
{
//  object FSM extends Enumeration
//  {
//    val FETCH = Value
//    val DECODE = Value
//    val EXECUTE = Value
//  }

  //chisel3 fsm
  val io = IO( new Bundle
  {
    //Input Instruction
    val inst = Input(UInt(Constants.INSTRUCTION_WIDTH.W))

    //Output to the DataStructure
    val ctrl = Output(new ctrl2data)


    //Interrupt Enable and Interrupt Flag bits
    val IE = Output(Bool())
    val IF = Output(Bool())

    //Stack control signals
    val stack_push = Output(Bool())
    val stack_pop = Output(Bool())

    //PC control signals
    val pc_inc = Output(Bool())
    val pc_jmp = Output(Bool())
    val pc_ret = Output(Bool())
    val pc_jmp_addr = Output((UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W)))
  })

  //TODO: ez nem jó -> hogy kell állapotgépet csinálni???
  val sFETCH :: sDECODE :: sEXECUTE :: Nil = Enum(3)
  val fsm = RegInit(sFETCH)

  val inst_reg = Reg(UInt(Constants.INSTRUCTION_WIDTH.W))

  val ctrl_signals = new ctrl2data

  val D = io.inst(Constants.INSTRUCTION_WIDTH - 1, Constants.INSTRUCTION_WIDTH - 4)   //15 downto 12
  val C = io.inst(Constants.INSTRUCTION_WIDTH - 5, Constants.INSTRUCTION_WIDTH - 8)   //11 downto 8
  val B = io.inst(Constants.INSTRUCTION_WIDTH - 9, Constants.INSTRUCTION_WIDTH - 12)  //7 downto 4
  val A = io.inst(Constants.INSTRUCTION_WIDTH - 13, Constants.INSTRUCTION_WIDTH - 16) //3 downto 0

  switch (fsm)
  {
    is (sFETCH)
    {
      inst_reg := io.inst
      //TODO: Itt mit csináljon az DATASTRUCTURE?? alu pass és registerfile írásengedélyezés tiltása?
      //Úgy álítom h ne csináljon semmit
    }
    is (sDECODE)
    {
      //Szétbontani utaítás típusokra(load, aritmatikai...)

      /////////////////////////////////////////////////////////////////////////////////
      //B type instruction:
      //|  1111  | rX/vezérlés | opkód  | rY/vezérlés |
      /////////////////////////////////////////////////////////////////////////////////
      when(D === OpCodes.REG_OP_PREFIX.U)
      {
        switch (B)
        {
          is (OPCODE_LD.U)
          {
            // | 1111 |   rX   |  1101  |    rY   |
            //Adatmemória olvasás indirekt címzéssel: rX <- DMEM[rY]
            io.ctrl.mux3sel := 1.U  //indirekt címzés
            io.ctrl.regs_b := A     //rY kiválasztás

            io.ctrl.mem_rd := 1.U   //memória olvasás művelet
            io.ctrl.mem_wr := 0.U

            io.ctrl.mux1sel := 1.U  //memória adat
            io.ctrl.regs_a := C     //rX cím
            io.ctrl.regs_we := 1.U  //regiszter írás

            io.pc_inc := 1.U        //PC inkrementálás
          }

          is (OPCODE_ST.U)
          {
            // | 1111 |   rX   |  1001  |    rY   |
            //Adatmemória írás indirekt címzéssel: DMEM[rY] <- rX
            io.ctrl.mux3sel := 1.U  //indirekt címzés
            io.ctrl.regs_b := A     //rY kiválasztás

            io.ctrl.mem_rd := 0.U
            io.ctrl.mem_wr := 1.U   //memória írás művelet

            io.ctrl.regs_a := C     //rX cím
            io.ctrl.regs_we := 0.U

            io.pc_inc := 1.U        //PC növelése
          }

          is (OPCODE_MOV.U)
          {
            // | 1111 |   rX   |  1100  |    rY   |
            //Adatmozgatás regiszterbõl regiszterbe: rX <- rY
            io.ctrl.mem_rd := 0.U
            io.ctrl.mem_wr := 0.U

            io.ctrl.alu_op := ALU_Ops.pass.U  //ALU művelet
            io.ctrl.regs_b := A               //rY
            io.ctrl.mux2sel := 1.U            //ALU 2 regiszter

            io.ctrl.mux1sel := 0.U            //ALU y -> RF din
            io.ctrl.regs_we := 1.U            //regiszter írás
            io.ctrl.regs_a := C               //rX

            io.pc_inc := 1.U        //PC növelése
          }

          is (OPCODE_ADD.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter hozzáadása regiszterhez átvitel nélkül: rX <- rX + rY
            io.ctrl.mem_rd := 0.U
            io.ctrl.mem_wr := 0.U

            io.ctrl.regs_b := A
            io.ctrl.mux2sel := 0.U

            io.ctrl.regs_a := C
            io.ctrl.regs_we := 1.U

            io.ctrl.alu_op := ALU_Ops.add.U
            io.ctrl.alu_flag.carry := 0.U
            io.ctrl.mux1sel := 0.U

            io.pc_inc := 1.U
          }
          is (OPCODE_ADC.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter hozzáadása regiszterhez átvitellel: rX <- rX + rY + C
            io.ctrl.mem_rd := 0.U
            io.ctrl.mem_wr := 0.U

            io.ctrl.regs_b := A
            io.ctrl.mux2sel := 0.U

            io.ctrl.regs_a := C
            io.ctrl.regs_we := 1.U

            io.ctrl.alu_op := ALU_Ops.add.U
            io.ctrl.alu_flag.carry := 1.U
            io.ctrl.mux1sel := 0.U

            io.pc_inc := 1.U
          }
          is (OPCODE_SUB.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter kivonása regiszterbõl átvitel nélkül: rX <- rX - rY
            io.ctrl.mem_rd := 0.U
            io.ctrl.mem_wr := 0.U

            io.ctrl.regs_b := A
            io.ctrl.mux2sel := 0.U

            io.ctrl.regs_a := C
            io.ctrl.regs_we := 1.U

            io.ctrl.alu_op := ALU_Ops.sub.U
            io.ctrl.alu_flag.carry := 0.U
            io.ctrl.mux1sel := 0.U

            io.pc_inc := 1.U
          }
          is (OPCODE_SBC.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter kivonása regiszterbõl átvitellel: rX <- rX - rY + C
            io.ctrl.mem_rd := 0.U
            io.ctrl.mem_wr := 0.U

            io.ctrl.regs_b := A
            io.ctrl.mux2sel := 0.U

            io.ctrl.regs_a := C
            io.ctrl.regs_we := 1.U

            io.ctrl.alu_op := ALU_Ops.sub.U
            io.ctrl.alu_flag.carry := 1.U
            io.ctrl.mux1sel := 0.U

            io.pc_inc := 1.U
          }

          is (OPCODE_CMP.U)
          {
            // | 1111 |   rX   |  1010  |    rY   |
            //Regiszter összehasonlítása regiszterrel: rX - rY
            io.ctrl.mem_rd := 0.U
            io.ctrl.mem_wr := 0.U

            io.ctrl.regs_b := A
            io.ctrl.mux2sel := 0.U

            io.ctrl.regs_a := C
            io.ctrl.regs_we := 0.U

            io.ctrl.alu_op := ALU_Ops.cmp.U

            io.pc_inc := 1.U
          }

          is (OPCODE_AND.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
          }
          is (OPCODE_OR.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
          }
          is (OPCODE_XOR.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
          }

          is (OPCODE_TST.U)
          {
            // | 1111 |   rX   |  1000  |    rY   |
          }

          is (OPCODE_SHIFT.U)
          {
            // | 1111 |   rX   |  0111  |  AIRD   |
          }

          is (OPCODE_CTRL.U)
          {
            // | 1111 | mûvelet|  1011  |    rY   |
          }
        }
      }
      /////////////////////////////////////////////////////////////////////////////////
      //A type instruction:
      //| opkód  | rX/vezérlés |   8 bites konstans   |
      /////////////////////////////////////////////////////////////////////////////////
      .otherwise
      {
        switch(D)
        {
          is (OPCODE_LD.U)
          {
            // | 1101 |   rX   | adatmemória cím  |
            //Adatmemória olvasás abszolút címzéssel: rX <- DMEM[addr]
            io.ctrl.mem_addr := Cat(B, A) //abszolút memória cím
            io.ctrl.mux3sel := 0.U        //abszolút címzés
            io.ctrl.mem_rd := 1.U         //memória olvasás
            io.ctrl.mem_wr := 0.U

            io.ctrl.mux1sel := 1.U        //memória data

            io.ctrl.regs_a := C           //rx cím
            io.ctrl.regs_we := 1.U        //regiszter írás

            io.pc_inc := 1.U              //PC növelése
          }

          is (OPCODE_ST.U)
          {
            // | 1001 |   rX   | adatmemória cím  |
            //Adatmemória írás abszolút címzéssel: DMEM[addr] <- rX
            io.ctrl.mem_addr := Cat(B, A) //abszolút memória cím
            io.ctrl.mux3sel := 0.U        //abszolút címzés
            io.ctrl.mem_wr := 1.U         //memória írás
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_a := C           //rx cím
            io.ctrl.regs_we := 0.U

            io.pc_inc := 1.U              //PC növelése
          }

          is (OPCODE_MOV.U)
          {
            // | 1100 |   rX   | 8 bites konstans |
            //Konstans betöltése regiszterbe: rX <- imm
            io.ctrl.mem_wr := 0.U
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_we := 1.U      //regiszter írás
            io.ctrl.regs_a := C

            io.ctrl.const := Cat(B, A)  //konstans
            io.ctrl.mux2sel := 1.U      //konstans -> ALU
            io.ctrl.alu_op := ALU_Ops.pass.U  //ALU művelet
            io.ctrl.mux1sel := 0.U      //ALU y -> RF din

            io.pc_inc := 1.U              //PC növelése
          }

          is (OPCODE_ADD.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans hozzáadása regiszterhez átvitel nélkül: rX <- rX + imm
            io.ctrl.mem_wr := 0.U
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_a := C     //rX
            io.ctrl.regs_we := 1.U  //regiszter írás

            io.ctrl.const := Cat(B, A)  //konstans
            io.ctrl.mux2sel := 1.U      //konstans -> alu
            io.ctrl.alu_flag.carry := 0.U //carry
            io.ctrl.alu_op := ALU_Ops.add.U //ALU művelet

            io.ctrl.mux1sel := 0.U  //alu y -> rf din

            io.pc_inc := 1.U              //PC növelése
          }
          is (OPCODE_ADC.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans hozzáadása regiszterhez átvitellel: rX <- rX + imm + C
            io.ctrl.mem_wr := 0.U
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_a := C     //rX
            io.ctrl.regs_we := 1.U  //regiszter írás

            io.ctrl.const := Cat(B, A)  //konstans
            io.ctrl.mux2sel := 1.U      //konstans -> alu
            io.ctrl.alu_flag.carry := 1.U //carry
            io.ctrl.alu_op := ALU_Ops.add.U //ALU művelet

            io.ctrl.mux1sel := 0.U  //alu y -> rf din

            io.pc_inc := 1.U              //PC növelése
          }
          is (OPCODE_SUB.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans kivonása regiszterbõl átvitel nélkül: rX <- rX - imm
            io.ctrl.mem_wr := 0.U
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_a := C     //rX
            io.ctrl.regs_we := 1.U  //regiszter írás

            io.ctrl.const := Cat(B, A)  //konstans
            io.ctrl.mux2sel := 1.U      //konstans -> alu
            io.ctrl.alu_flag.carry := 0.U //carry
            io.ctrl.alu_op := ALU_Ops.sub.U //ALU művelet

            io.ctrl.mux1sel := 0.U  //alu y -> rf din

            io.pc_inc := 1.U              //PC növelése
          }
          is (OPCODE_SBC.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans kivonása regiszterbõl átvitellel: rX <- rX - imm + C
            io.ctrl.mem_wr := 0.U
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_a := C     //rX
            io.ctrl.regs_we := 1.U  //regiszter írás

            io.ctrl.const := Cat(B, A)  //konstans
            io.ctrl.mux2sel := 1.U      //konstans -> alu
            io.ctrl.alu_flag.carry := 1.U //carry
            io.ctrl.alu_op := ALU_Ops.sub.U //ALU művelet

            io.ctrl.mux1sel := 0.U  //alu y -> rf din

            io.pc_inc := 1.U              //PC növelése
          }

          is (OPCODE_CMP.U)
          {
            // | 1010 |   rX   | 8 bites konstans |
            //Regiszter összehasonlítása konstanssal: rX - imm
            io.ctrl.mem_wr := 0.U
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_we := 0.U

            io.ctrl.alu_op := ALU_Ops.cmp.U

            io.ctrl.const := Cat(B, A)
            io.ctrl.mux2sel := 1.U

            io.ctrl.regs_a := C

            io.pc_inc := 1.U              //PC növelése
          }

          is (OPCODE_AND.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
            //Bitenkénti ÉS konstanssal: rX <- rX & imm
            io.ctrl.mem_wr := 0.U
            io.ctrl.mem_rd := 0.U

            io.ctrl.regs_a := C
            io.ctrl.regs_we := 1.U
            io.ctrl.mux1sel := 0.U

            io.ctrl.alu_op := ALU_Ops.and.U

            io.ctrl.const := Cat(B, A)
            io.ctrl.mux2sel := 1.U

            io.pc_inc := 1.U
          }
          is (OPCODE_OR.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
          }
          is (OPCODE_XOR.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
          }

          is (OPCODE_TST.U)
          {
            // | 1000 |   rX   | 8 bites konstans |
          }

          is (OPCODE_CTRL.U)
          {
            // | 1011 | mûvelet|     00000000     |
          }
        }
      }

      //TODO: Itt mit csináljon az DATASTRUCTURE??
    }
    is (sEXECUTE)
    {
      //várok egy órajelet
      fsm := sFETCH
    }
  }
}
