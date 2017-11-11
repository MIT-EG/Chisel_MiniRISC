package Controller

import Chisel.Enum
import Common.{ALU_Ops, Constants, ctrl2data}
import chisel3._
import chisel3.util._
import Controller.OpCodes._

class FSM extends Chisel.Module
{
  val io = IO( new Bundle
  {
    //Input Instruction
    val inst = Input(UInt(Constants.INSTRUCTION_WIDTH.W))

    //Control to Data
    val flag_in = Input(new Common.flags)
    val alu_y = Input(UInt(Constants.DATA_WIDTH.W))

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

    val valid = Output(Bool())
  })

  //FSM with enum
  val sFETCH :: sDECODE :: sEXECUTE :: Nil = Enum(3)
  val fsm = RegInit(sFETCH)

  //Instruction register from program memory
  val inst_reg = Reg(UInt(Constants.INSTRUCTION_WIDTH.W))


  val ctrl_signals = new ctrl2data

  //4bit groups of the instruction
  val D = inst_reg(Constants.INSTRUCTION_WIDTH - 1, Constants.INSTRUCTION_WIDTH - 4)   //15 downto 12
  val C = inst_reg(Constants.INSTRUCTION_WIDTH - 5, Constants.INSTRUCTION_WIDTH - 8)   //11 downto 8
  val B = inst_reg(Constants.INSTRUCTION_WIDTH - 9, Constants.INSTRUCTION_WIDTH - 12)  //7 downto 4
  val A = inst_reg(Constants.INSTRUCTION_WIDTH - 13, Constants.INSTRUCTION_WIDTH - 16) //3 downto 0

  //DataStructure memory operation enum
  val sMEM_NONE :: sABSOLUTE_WR :: sABSOLUTE_RD :: sINDIRECT_WR :: sINDIRECT_RD :: Nil = Enum(5)
  val mem_op = RegInit(sMEM_NONE)

  //DataStructure RF(groups: 1OP - 2OP; WR - RD; MEM - ALU)
  val sRF_NONE :: s1OP_WR_MEM :: s1OP_WR_ALU :: s1OP_RD_MEM :: s1OP_RD_ALU :: s2OP_WR_MEM :: s2OP_WR_ALU :: s2OP_RD_MEM :: s2OP_RD_ALU :: Nil = Enum(9)
  val rf_op = RegInit(sRF_NONE)
  val regs_a_reg = RegInit(UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W ), 0.U)
  val regs_b_reg = RegInit(UInt( log2Ceil( Constants.NUMBER_OF_REGISTERS ).W ), 0.U)

  //DataStructure ALU
  val alu_op = RegInit(ALU_Ops.pass.U)
  val alu_flag = Reg(new Common.flags)

  //Constans + mem addr
  val const_reg = RegInit(UInt(Constants.DATA_WIDTH.W), 0.U)
  val mem_addr_reg = RegInit(UInt(log2Ceil(Constants.DATA_MEMORY_SIZE).W), 0.U)

  //PC
  val sINC :: sJMP :: sRET :: Nil = Enum(3)
  val pc_op = RegInit(sINC)
  val pc_jmp_reg = RegInit(UInt(log2Ceil(Constants.PROGRAM_MEMORY_SIZE).W), 0.U)

  //Stack
  val sSTACK_NONE :: sPUSH :: sPOP :: Nil = Enum(3)
  val stack_op = RegInit(sSTACK_NONE)

  //IE, IF
  val IE_reg = RegInit(Bool(), 0.U)
  val IF_reg = RegInit(Bool(), 0.U)

  switch (fsm)
  {
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //FETCH
    ///////////////////////////////////////////////////////////////////////////////////////////////
    is (sFETCH)
    {
      inst_reg := io.inst

      //TODO: Itt mit csináljon az DATASTRUCTURE?? -> Masszívan semmit
      io.stack_push := 0.U
      io.stack_pop := 0.U

      io.pc_inc := 0.U
      io.pc_jmp := 0.U
      io.pc_ret := 0.U

      io.ctrl.regs_we := 0.U
      io.ctrl.mem_wr := 0.U
      io.ctrl.mem_rd := 0.U

      io.valid := 0.U

      fsm := sDECODE
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //DECODE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    is (sDECODE)
    {
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

            mem_op := sINDIRECT_RD
            rf_op := s2OP_WR_MEM
            regs_a_reg := C
            regs_b_reg := A
            //alu_op
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }

          is (OPCODE_ST.U)
          {
            // | 1111 |   rX   |  1001  |    rY   |
            //Adatmemória írás indirekt címzéssel: DMEM[rY] <- rX

            mem_op := sINDIRECT_WR
            rf_op := s2OP_RD_MEM
            regs_a_reg := C
            regs_b_reg := A
            //alu_op
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }

          is (OPCODE_MOV.U)
          {
            // | 1111 |   rX   |  1100  |    rY   |
            //Adatmozgatás regiszterbõl regiszterbe: rX <- rY

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.pass.U
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }

          is (OPCODE_ADD.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter hozzáadása regiszterhez átvitel nélkül: rX <- rX + rY

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.add.U
            alu_flag.carry := 0.U
            pc_op := sINC
            stack_op := sSTACK_NONE
          }
          is (OPCODE_ADC.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter hozzáadása regiszterhez átvitellel: rX <- rX + rY + C

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.add.U
            alu_flag.carry := 1.U
            pc_op := sINC
            stack_op := sSTACK_NONE
          }
          is (OPCODE_SUB.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter kivonása regiszterbõl átvitel nélkül: rX <- rX - rY

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.sub.U
            alu_flag.carry := 0.U
            pc_op := sINC
            stack_op := sSTACK_NONE
          }
          is (OPCODE_SBC.U)
          {
            // | 1111 |   rX   |  00SC  |    rY   |
            //Regiszter kivonása regiszterbõl átvitellel: rX <- rX - rY + C

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.sub.U
            alu_flag.carry := 1.U
            pc_op := sINC
            stack_op := sSTACK_NONE
          }

          is (OPCODE_CMP.U)
          {
            // | 1111 |   rX   |  1010  |    rY   |
            //Regiszter összehasonlítása regiszterrel: rX - rY

            mem_op := sMEM_NONE
            rf_op := s2OP_RD_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.cmp.U
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }

          is (OPCODE_AND.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
            //Bitenkénti ÉS regiszterrel: rX <- rX & rY

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.and.U
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }
          is (OPCODE_OR.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
            //Bitenkénti VAGY regiszterrel: rX <- rX | rY

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.or.U
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }
          is (OPCODE_XOR.U)
          {
            // | 1111 |   rX   |  01AB  |    rY   |
            //Bitenkénti XOR regiszterrel: rX <- rX ^ rY

            mem_op := sMEM_NONE
            rf_op := s2OP_WR_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.xor.U
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }

          is (OPCODE_TST.U)
          {
            // | 1111 |   rX   |  1000  |    rY   |
            //Bittesztelés regiszterrel: rX & rY

            mem_op := sMEM_NONE
            rf_op := s2OP_RD_ALU
            regs_a_reg := C
            regs_b_reg := A
            alu_op := ALU_Ops.and.U
            //alu_flag
            pc_op := sINC
            stack_op := sSTACK_NONE
          }

          is (OPCODE_SHIFT.U)
          {
            // | 1111 |   rX   |  0111  |  AIRD   |
            //*  D: irány kiválasztása (0: balra, 1: jobbra)                               *
            //*  R: mûvelet kiválasztása (0: shiftelés, 1: forgatás)                       *
            //*  I: a beshiftelt bit értéke/kiválasztása (0: 0/kishiftelt bit, 1: 1/carry) *
            //*  A: a shiftelés típusa (0: normál, 1: aritmetikai)

            mem_op := sMEM_NONE
            rf_op := s2OP_RD_ALU
            regs_a_reg := C
            regs_b_reg := A
            pc_op := sINC
            stack_op := sSTACK_NONE

            when(A(2) === 1.U)
            {
              alu_flag.carry := 1.U
            } .otherwise
            {
              alu_flag.carry := 1.U
            }
            switch(A(1, 0))
            {
              is(SHIFT_SHL.U)
              {
                when(A(3) === 1.U)
                {
                  alu_op := ALU_Ops.ashl.U
                } .otherwise
                {
                  alu_op := ALU_Ops.lshl.U
                }
              }
              is(SHIFT_SHR.U)
              {
                when(A(3) === 1.U)
                {
                  alu_op := ALU_Ops.ashr.U
                } .otherwise
                {
                  alu_op := ALU_Ops.lshr.U
                }
              }
              is(SHIFT_ROL.U)
              {
                alu_op := ALU_Ops.rol.U
              }
              is(SHIFT_ROR.U)
              {
                alu_op := ALU_Ops.ror.U
              }
            }
          }

          is (OPCODE_CTRL.U)
          {
            // | 1011 | mûvelet|     00000000     |
            mem_op := sMEM_NONE
            rf_op := s2OP_RD_ALU
            switch(C)
            {
              is(CTRL_JMP.U)
              {
                //JMP (rY) - Feltétel nélküli ugrás (PC <- rY)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                pc_op := sJMP
                stack_op := sSTACK_NONE
              }
              is(CTRL_JZ.U)
              {
                //JZ  (rY) - Ugrás, ha a Z flag 1   (PC <- rY, ha Z=1)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.zero === 1.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNZ.U)
              {
                //JNZ (rY) - Ugrás, ha a Z flag 0   (PC <- rY, ha Z=0)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.zero === 0.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JC.U)
              {
                //JC  (rY) - Ugrás, ha a C flag 1   (PC <- rY, ha C=1)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.carry === 1.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNC.U)
              {
                //JNC (rY) - Ugrás, ha a C flag 0   (PC <- rY, ha C=0)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.carry === 0.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JN.U)
              {
                //JN  (rY) - Ugrás, ha az N flag 1  (PC <- rY, ha N=1)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.negative === 1.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNN.U)
              {
                //JNN (rY) - Ugrás, ha az N flag 0  (PC <- rY, ha N=0)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.negative === 0.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JV.U)
              {
                //JV  (rY) - Ugrás, ha a V flag 1   (PC <- rY, ha V=1)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.overflow === 1.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNV.U)
              {
                //JNV (rY) - Ugrás, ha a V flag 0   (PC <- rY, ha V=0)

                regs_b_reg := A
                pc_jmp_reg := io.alu_y
                when(io.flag_in.overflow === 0.U)
                {
                  pc_op := sJMP
                }   .otherwise
                {
                  pc_op := sINC
                }
                stack_op := sSTACK_NONE
              }
              is(CTRL_JSR.U)
              {
                //JSR (rY) - Szubrutinhívás         (stack <- PC <- rY)
                pc_op := sJMP
                stack_op := sPOP
              }
            }
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

            mem_op := sABSOLUTE_RD
            rf_op := s1OP_WR_MEM
            regs_a_reg := C
            //regs_b_reg
            //alu_op
            //alu_flag
            //const_reg
            mem_addr_reg := Cat(B, A)
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }

          is (OPCODE_ST.U)
          {
            // | 1001 |   rX   | adatmemória cím  |
            //Adatmemória írás abszolút címzéssel: DMEM[addr] <- rX

            mem_op := sABSOLUTE_WR
            rf_op := s1OP_RD_MEM
            regs_a_reg := C
            //regs_b_reg
            //alu_op
            //alu_flag
            //const_reg
            mem_addr_reg := Cat(B, A)
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }

          is (OPCODE_MOV.U)
          {
            // | 1100 |   rX   | 8 bites konstans |
            //Konstans betöltése regiszterbe: rX <- imm

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.pass.U
            //alu_flag
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }

          is (OPCODE_ADD.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans hozzáadása regiszterhez átvitel nélkül: rX <- rX + imm

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.add.U
            alu_flag.carry := 0.U
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }
          is (OPCODE_ADC.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans hozzáadása regiszterhez átvitellel: rX <- rX + imm + C

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.add.U
            alu_flag.carry := 1.U
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }
          is (OPCODE_SUB.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans kivonása regiszterbõl átvitel nélkül: rX <- rX - imm

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.sub.U
            alu_flag.carry := 0.U
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }
          is (OPCODE_SBC.U)
          {
            // | 00SC |   rX   | 8 bites konstans |
            //Konstans kivonása regiszterbõl átvitellel: rX <- rX - imm + C

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.sub.U
            alu_flag.carry := 1.U
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }

          is (OPCODE_CMP.U)
          {
            // | 1010 |   rX   | 8 bites konstans |
            //Regiszter összehasonlítása konstanssal: rX - imm

            mem_op := sMEM_NONE
            rf_op := s1OP_RD_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.cmp.U
            //alu_flag.carry
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }

          is (OPCODE_AND.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
            //Bitenkénti ÉS konstanssal: rX <- rX & imm

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.and.U
            //alu_flag.carry
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }
          is (OPCODE_OR.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
            //Bitenkénti VAGY konstanssal: rX <- rX | imm

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.or.U
            //alu_flag.carry
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }
          is (OPCODE_XOR.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
            //Bitenkénti XOR konstanssal: rX <- rX ^ imm

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.xor.U
            //alu_flag.carry
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }
          is(OPCODE_SWP.U)
          {
            // | 01AB |   rX   | 8 bites konstans |
            //Alsó/felsõ 4 bit felcserélése: rX <- {rX[3:0], rX[7:4]}

            mem_op := sMEM_NONE
            rf_op := s1OP_WR_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.swp.U
            //alu_flag.carry
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }

          is (OPCODE_TST.U)
          {
            // | 1000 |   rX   | 8 bites konstans |
            //Bittesztelés konstanssal: rX & imm

            mem_op := sMEM_NONE
            rf_op := s1OP_RD_ALU
            regs_a_reg := C
            //regs_b_reg
            alu_op := ALU_Ops.and.U
            //alu_flag.carry
            const_reg := Cat(B, A)
            //mem_addr_reg
            pc_op := sINC
            //pc_addr_reg
            stack_op := sSTACK_NONE
            //IE_reg
            //IF_reg
          }

          is (OPCODE_CTRL.U)
          {
            // | 1011 | mûvelet|     00000000     |
            switch(C)
            {
              is(CTRL_JMP.U)
              {
                //JMP addr - Feltétel nélküli ugrás (PC <- addr)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                pc_op := sJMP
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JZ.U)
              {
                //JZ  addr - Ugrás, ha a Z flag 1   (PC <- addr, ha Z=1)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.zero === 1.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNZ.U)
              {
                //JNZ addr - Ugrás, ha a Z flag 0   (PC <- addr, ha Z=0)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.zero === 0.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JC.U)
              {
                //JC  addr - Ugrás, ha a C flag 1   (PC <- addr, ha C=1)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.carry === 1.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNC.U)
              {
                //JNC addr - Ugrás, ha a C flag 0   (PC <- addr, ha C=0)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.carry === 0.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JN.U)
              {
                //JN  addr - Ugrás, ha az N flag 1  (PC <- addr, ha N=1)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.negative === 1.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNN.U)
              {
                //JNN addr - Ugrás, ha az N flag 0  (PC <- addr, ha N=0)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.negative === 0.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JV.U)
              {
                //JV  addr - Ugrás, ha a V flag 1   (PC <- addr, ha V=1)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.overflow === 1.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JNV.U)
              {
                //JNV addr - Ugrás, ha a V flag 0   (PC <- addr, ha V=0)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                when(io.flag_in.overflow === 0.U)
                {
                  pc_op := sJMP
                } .otherwise
                {
                  pc_op := sINC
                }
                pc_jmp_reg := Cat(B, A)
                stack_op := sSTACK_NONE
              }
              is(CTRL_JSR.U)
              {
                //JSR addr - Szubrutinhívás         (stack <- PC <- addr)

                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                pc_op := sJMP
                pc_jmp_reg := Cat(B, A)
                stack_op := sPUSH
              }
              is(CTRL_RTS.U)
              {
                //RTS - Visszatérés szubrutinból    (PC <- stack)
                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                pc_op := sRET
                stack_op := sPOP
              }
              is(CTRL_RTI.U)
              {
                //RTI - Visszatérés megszakításból  (PC,Z,C,N,V,IE <- stack)
                //TODO: Itt nincs különbség RTS és RTI között, és IE-t nem állítja vissza
                mem_op := sMEM_NONE
                rf_op := sRF_NONE
                pc_op := sRET
                stack_op := sPOP
              }
              is(CTRL_CLI.U)
              {
                //CLI - Megszakítások tiltása       (IE <- 0)
                IE_reg := 0.U
              }
              is(CTRL_STI.U)
              {
                //STI - Megszakítások engedélyezése (IE <- 1)
                IE_reg := 1.U
              }
            }
          }
        }
      }

      //TODO: Itt mit csináljon az DATASTRUCTURE?? -> Masszívan semmit
      io.stack_push := 0.U
      io.stack_pop := 0.U

      io.pc_inc := 0.U
      io.pc_jmp := 0.U
      io.pc_ret := 0.U

      io.ctrl.regs_we := 0.U
      io.ctrl.mem_wr := 0.U
      io.ctrl.mem_rd := 0.U

      io.valid := 0.U

      //NEXT STATE
      fsm := sEXECUTE
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //EXECUTE
    ///////////////////////////////////////////////////////////////////////////////////////////////
    is (sEXECUTE)
    {
      ///////////////////////////////////////////
      //MEMÓRIA MŰVELET
      ///////////////////////////////////////////
      switch(mem_op)
      {
        is(sABSOLUTE_RD)
        {
          io.ctrl.mem_rd := 1.U
          io.ctrl.mem_wr := 0.U

          io.ctrl.mem_addr := mem_addr_reg
          io.ctrl.mux3sel := 0.U
        }
        is(sABSOLUTE_WR)
        {
          io.ctrl.mem_rd := 0.U
          io.ctrl.mem_wr := 1.U

          io.ctrl.mem_addr := mem_addr_reg
          io.ctrl.mux3sel := 0.U
        }
        is(sINDIRECT_RD)
        {
          io.ctrl.mem_rd := 1.U
          io.ctrl.mem_wr := 0.U

          io.ctrl.mux3sel := 1.U
          io.ctrl.regs_a := regs_a_reg
        }
        is(sINDIRECT_WR)
        {
          io.ctrl.mem_rd := 0.U
          io.ctrl.mem_wr := 1.U

          io.ctrl.mux3sel := 1.U
          io.ctrl.regs_a := regs_a_reg
        }
        is(sMEM_NONE)
        {
          io.ctrl.mem_rd := 0.U
          io.ctrl.mem_wr := 0.U
        }
      }

      ///////////////////////////////////////////
      //REGISTER FILE MŰVELET
      ///////////////////////////////////////////
      switch(rf_op)
      {
        //1 Operand
        is(s1OP_RD_ALU)
        {
          io.ctrl.regs_a := regs_a_reg
          io.ctrl.const := const_reg
          io.ctrl.mux2sel := 1.U

          io.ctrl.regs_we := 0.U
        }
        is(s1OP_RD_MEM)
        {
          io.ctrl.regs_a := regs_a_reg

          io.ctrl.regs_we := 0.U
        }
        is(s1OP_WR_ALU)
        {
          io.ctrl.regs_a := regs_a_reg
          io.ctrl.const := const_reg
          io.ctrl.mux2sel := 1.U

          io.ctrl.regs_we := 1.U
          io.ctrl.mux1sel := 0.U
        }
        is(s1OP_WR_MEM)
        {
          io.ctrl.regs_a := regs_a_reg

          io.ctrl.regs_we := 1.U
          io.ctrl.mux1sel := 1.U
        }

        //2 Operand
        is(s2OP_RD_ALU)
        {
          io.ctrl.regs_a := regs_a_reg
          io.ctrl.regs_b := regs_b_reg

          io.ctrl.regs_we := 0.U

          io.ctrl.mux2sel := 0.U
        }
        is(s2OP_RD_MEM)
        {
          io.ctrl.regs_a := regs_a_reg
          io.ctrl.regs_b := regs_b_reg

          io.ctrl.regs_we := 0.U
        }
        is(s2OP_WR_ALU)
        {
          io.ctrl.regs_a := regs_a_reg
          io.ctrl.regs_b := regs_b_reg

          io.ctrl.regs_we := 1.U
          io.ctrl.mux1sel := 0.U

          io.ctrl.mux2sel := 0.U
        }
        is(s2OP_WR_MEM)
        {
          io.ctrl.regs_a := regs_a_reg
          io.ctrl.regs_b := regs_b_reg

          io.ctrl.regs_we := 1.U
          io.ctrl.mux1sel := 1.U
        }
        is(sRF_NONE)
        {
          io.ctrl.regs_we := 0.U
        }
      }

      ///////////////////////////////////////////
      //ALU MŰVELET
      ///////////////////////////////////////////
      io.ctrl.alu_op := alu_op
      io.ctrl.alu_flag := alu_flag

      ///////////////////////////////////////////
      //PROGRAM COUNTER MŰVELET
      ///////////////////////////////////////////
      switch(pc_op)
      {
        is(sINC)
        {
          io.pc_inc := 1.U
          io.pc_jmp := 0.U
          io.pc_ret := 0.U
        }
        is(sJMP)
        {
          io.pc_inc := 0.U
          io.pc_jmp := 1.U
          io.pc_ret := 0.U
          io.pc_jmp_addr := pc_jmp_reg
        }
        is(sRET)
        {
          io.pc_inc := 0.U
          io.pc_jmp := 0.U
          io.pc_ret := 1.U
        }
      }

      ///////////////////////////////////////////
      //STACK MŰVELET
      ///////////////////////////////////////////
      switch(stack_op)
      {
        is(sSTACK_NONE)
        {
          io.stack_push := 0.U
          io.stack_pop := 0.U
        }
        is(sPOP)
        {
          io.stack_push := 0.U
          io.stack_pop := 1.U
        }
        is(sPUSH)
        {
          io.stack_push := 1.U
          io.stack_pop := 0.U
        }
      }

      ///////////////////////////////////////////
      //IE, IF
      ///////////////////////////////////////////
      io.IE := IE_reg
      io.IF := IF_reg

      io.valid := 1.U

      //NEXT STATE
      fsm := sFETCH
    }
  }
}