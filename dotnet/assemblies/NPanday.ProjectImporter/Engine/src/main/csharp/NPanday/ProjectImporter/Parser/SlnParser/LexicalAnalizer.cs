#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion
using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Parser.SlnParser
{
    public class LexicalAnalizer
    {
        SemanticalData[] tokenData;

        

        #region Constructors

        public LexicalAnalizer(FileInfo solutionFile)
            : this(solutionFile.FullName)
        {
        }


        public LexicalAnalizer(string solutionFile)
        {
            StringBuilder data = new StringBuilder();
            using (StreamReader reader = new StreamReader(solutionFile))
            {
                data.Append(reader.ReadToEnd());
            }

            if (data.Length > 0)
            {
                data.Append("\n");
                data = data.Replace("\r", ""); // remove the /r, for easy parsing
                char[] arr = new char[data.Length];
                data.CopyTo(0, arr, 0, data.Length);
                tokenData = Tokenize(arr);
            }

            
        }

        #endregion


        #region Parser Algorithms

        static SemanticalData[] Tokenize(char[] data)
        {
            List<SemanticalData> list = new List<SemanticalData>();
            StringBuilder sb = new StringBuilder();
            int pointer = 0;
            int len = data.Length;

            LexanState state = LexanState.Start_State;

            while (pointer < len)
            {
                char c = data[pointer];
                switch (state)
                {
                    #region Keyword Tokens

                    #region Start State

                    case LexanState.Start_State:

                        if (c == '\n')
                        {
                            list.Add(new SemanticalData(Semantics.EOL));
                        }
                        else if (char.IsWhiteSpace(c))
                        {
                            // same state;
                            // but move to the next char, dont call continue
                        }
                        else if (c == 'P')
                        {
                            state = LexanState.Project_P;
                        }
                        else if (c == 'E')
                        {
                            state = LexanState.End_E;
                        }
                        else if (c == 'G')
                        {
                            state = LexanState.Global_G;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;

                    #endregion

                    #region Project Chars State
                    case LexanState.Project_P:
                        if (c == 'r')
                        {
                            state = LexanState.Project_r;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Project_r:
                        if (c == 'o')
                        {
                            state = LexanState.Project_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Project_o:
                        if (c == 'j')
                        {
                            state = LexanState.Project_j;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Project_j:
                        if (c == 'e')
                        {
                            state = LexanState.Project_e;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Project_e:
                        if (c == 'c')
                        {
                            state = LexanState.Project_c;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Project_c:
                        if (c == 't')
                        {
                            state = LexanState.Project_t;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Project_t: // Token Terminal state
                        if (c == 'S')
                        {
                            state = LexanState.ProjectSection_S;
                        }
                        else if(!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.PROJECT));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }

                        break;
                    #endregion

                    #region ProjectSection Chars State
                    // ProjectSection chars will start at 'Section' 
                    // since 'Project' word is coverd by Project chars
                    case LexanState.ProjectSection_S:
                        if (c == 'e')
                        {
                            state = LexanState.ProjectSection_e;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.ProjectSection_e:
                        if (c == 'c')
                        {
                            state = LexanState.ProjectSection_c;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.ProjectSection_c:
                        if (c == 't')
                        {
                            state = LexanState.ProjectSection_t;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.ProjectSection_t:
                        if (c == 'i')
                        {
                            state = LexanState.ProjectSection_i;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.ProjectSection_i:
                        if (c == 'o')
                        {
                            state = LexanState.ProjectSection_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.ProjectSection_o:
                        if (c == 'n')
                        {
                            state = LexanState.ProjectSection_n;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.ProjectSection_n: // Token Terminal State
                        if (!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.PROJECT_SECTION));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                    #endregion

                    #region Global Chars State
                    // Global chars
                    case LexanState.Global_G:
                        if (c == 'l')
                        {
                            state = LexanState.Global_l;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Global_l:
                        if (c == 'o')
                        {
                            state = LexanState.Global_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Global_o:
                        if (c == 'b')
                        {
                            state = LexanState.Global_b;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Global_b:
                        if (c == 'a')
                        {
                            state = LexanState.Global_a;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Global_a: 
                        if (c == 'l')
                        {
                            state = LexanState.Global_l2;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.Global_l2: // Token Terminal State
                        if (c == 'S')
                        {
                            state = LexanState.GlobalSection_S;
                        }
                        else if (!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.GLOBAL));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    #endregion

                    #region GlobalSection Chars

                    // GlobalSection chars will start at 'Section' 
                    // since 'Global' word is coverd by Global chars
                    case LexanState.GlobalSection_S:
                        if (c == 'e')
                        {
                            state = LexanState.GlobalSection_e;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.GlobalSection_e:
                        if (c == 'c')
                        {
                            state = LexanState.GlobalSection_c;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.GlobalSection_c:
                        if (c == 't')
                        {
                            state = LexanState.GlobalSection_t;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.GlobalSection_t:
                        if (c == 'i')
                        {
                            state = LexanState.GlobalSection_i;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.GlobalSection_i:
                        if (c == 'o')
                        {
                            state = LexanState.GlobalSection_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.GlobalSection_o:
                        if (c == 'n')
                        {
                            state = LexanState.GlobalSection_n;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.GlobalSection_n: // Token Terminal State
                        if (!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.GLOBAL_SECTION));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                    #endregion


                    #region End Chars
                    case LexanState.End_E:
                        if (c == 'n')
                        {
                            state = LexanState.End_n;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.End_n:
                        if (c == 'd')
                        {
                            state = LexanState.End_d;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.End_d: // Token Terminal state
                        if (c == 'P')
                        {
                            state = LexanState.EndProject_P;
                        }
                        else if (c == 'G')
                        {
                            state = LexanState.EndGlobal_G;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;

                    #endregion

                    #region EndProject Chars
                    case LexanState.EndProject_P:
                        if (c == 'r')
                        {
                            state = LexanState.EndProject_r;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProject_r:
                        if (c == 'o')
                        {
                            state = LexanState.EndProject_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProject_o:
                        if (c == 'j')
                        {
                            state = LexanState.EndProject_j;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProject_j:
                        if (c == 'e')
                        {
                            state = LexanState.EndProject_e;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProject_e:
                        if (c == 'c')
                        {
                            state = LexanState.EndProject_c;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProject_c:
                        if (c == 't')
                        {
                            state = LexanState.EndProject_t;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProject_t: // Token Terminal state
                        if (c == 'S')
                        {
                            state = LexanState.EndProjectSection_S;
                        }
                        else if (!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.END_PROJECT));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    #endregion

                    #region EndProjectSection Chars
                    // EndProjectSection charswill start at 'Section' 
                    // since 'EndProject' word is coverd by EndProject chars
                    case LexanState.EndProjectSection_S:
                        if (c == 'e')
                        {
                            state = LexanState.EndProjectSection_e;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProjectSection_e:
                        if (c == 'c')
                        {
                            state = LexanState.EndProjectSection_c;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProjectSection_c:
                        if (c == 't')
                        {
                            state = LexanState.EndProjectSection_t;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProjectSection_t:
                        if (c == 'i')
                        {
                            state = LexanState.EndProjectSection_i;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProjectSection_i:
                        if (c == 'o')
                        {
                            state = LexanState.EndProjectSection_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProjectSection_o:
                        if (c == 'n')
                        {
                            state = LexanState.EndProjectSection_n;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndProjectSection_n: // Token Terminal State
                        if (!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.END_PROJECT_SECTION));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                    #endregion

                    #region EndGlobal Chars


                    case LexanState.EndGlobal_G:
                        if (c == 'l')
                        {
                            state = LexanState.EndGlobal_l;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobal_l:
                        if (c == 'o')
                        {
                            state = LexanState.EndGlobal_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobal_o:
                        if (c == 'b')
                        {
                            state = LexanState.EndGlobal_b;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobal_b:
                        if (c == 'a')
                        {
                            state = LexanState.EndGlobal_a;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobal_a:
                        if (c == 'l')
                        {
                            state = LexanState.EndGlobal_l2;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobal_l2: // Token Terminal state
                        if (c == 'S')
                        {
                            state = LexanState.EndGlobalSection_S;
                        }
                        else if (!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.END_GLOBAL));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;

                    #endregion

                    #region EndGlobalSection Chars


                    // EndGlobalSection charswill start at 'Section' 
                    // since 'EndGlobal' word is coverd by EndGlobal chars
                    case LexanState.EndGlobalSection_S:
                        if (c == 'e')
                        {
                            state = LexanState.EndGlobalSection_e;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobalSection_e:
                        if (c == 'c')
                        {
                            state = LexanState.EndGlobalSection_c;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobalSection_c:
                        if (c == 't')
                        {
                            state = LexanState.EndGlobalSection_t;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobalSection_t:
                        if (c == 'i')
                        {
                            state = LexanState.EndGlobalSection_i;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobalSection_i:
                        if (c == 'o')
                        {
                            state = LexanState.EndGlobalSection_o;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobalSection_o:
                        if (c == 'n')
                        {
                            state = LexanState.EndGlobalSection_n;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                        break;
                    case LexanState.EndGlobalSection_n: // Token Terminal State
                        if (!char.IsLetterOrDigit(c))
                        {
                            sb.Length = 0;
                            list.Add(new SemanticalData(Semantics.END_GLOBAL_SECTION));
                            state = LexanState.Start_State;
                            continue;
                        }
                        else
                        {
                            state = LexanState.STRING_VALUE;
                            continue;
                        }
                    

                    #endregion

                    #endregion

                    #region Value Tokens



                    case LexanState.COMMA:
                        list.Add(new SemanticalData(Semantics.COMMA));
                        sb.Length = 0;
                        state = LexanState.Start_State;
                        continue;
                    case LexanState.OPEN_PARENTHESIS:
                        list.Add(new SemanticalData(Semantics.OPEN_PARENTHESIS));
                        sb.Length = 0;
                        state = LexanState.Start_State;
                        continue;
                    case LexanState.CLOSE_PARENTHESIS:
                        list.Add(new SemanticalData(Semantics.CLOSE_PARENTHESIS));
                        sb.Length = 0;
                        state = LexanState.Start_State;
                        continue;
                    case LexanState.EQUALS:
                        list.Add(new SemanticalData(Semantics.EQUALS));
                        sb.Length = 0;
                        state = LexanState.Start_State;
                        continue;
            
                    case LexanState.STRING_VALUE:
                        
                        if (c == '\"')
                        {
                            if (sb.Length > 0 && !string.IsNullOrEmpty(sb.ToString().Trim()))
                            {
                                list.Add(new SemanticalData(Semantics.STRING_VALUE, sb.ToString()));
                                sb.Length = 0;
                            }
                            state = LexanState.QUOTE_1;
                        }
                        else if (c == '\n')
                        {
                            if (sb.Length > 0 && !string.IsNullOrEmpty(sb.ToString().Trim()))
                            {
                                list.Add(new SemanticalData(Semantics.STRING_VALUE, sb.ToString()));
                                sb.Length = 0;
                            }
                            list.Add(new SemanticalData(Semantics.EOL));
                            state = LexanState.Start_State;
                        }
                        else if (c == '(')
                        {
                            if (sb.Length > 0 && !string.IsNullOrEmpty(sb.ToString().Trim()))
                            {
                                list.Add(new SemanticalData(Semantics.STRING_VALUE, sb.ToString()));
                                sb.Length = 0;
                            }
                            state = LexanState.OPEN_PARENTHESIS;
                        }
                        else if (c == ')')
                        {
                            if (sb.Length > 0 && !string.IsNullOrEmpty(sb.ToString().Trim()))
                            {
                                list.Add(new SemanticalData(Semantics.STRING_VALUE, sb.ToString()));
                                sb.Length = 0;
                            }
                            state = LexanState.CLOSE_PARENTHESIS;
                        }
                        else if (c == ',')
                        {
                            if (sb.Length > 0 && !string.IsNullOrEmpty(sb.ToString().Trim()))
                            {
                                list.Add(new SemanticalData(Semantics.STRING_VALUE, sb.ToString()));
                                sb.Length = 0;
                            }
                            state = LexanState.COMMA;
                        }
                        else if(c == '=')
                        {
                            if (sb.Length > 0 && !string.IsNullOrEmpty(sb.ToString().Trim()))
                            {
                                list.Add(new SemanticalData(Semantics.STRING_VALUE, sb.ToString()));
                                sb.Length = 0;
                            }
                            state = LexanState.EQUALS;
                        }
                        break;



                    case LexanState.QUOTE_1:
                        sb.Length = 0;
                        state = LexanState.QUOTE_DATA;
                        continue;


                    case LexanState.QUOTE_DATA:
                        if (c == '\"')
                        {
                            list.Add(new SemanticalData(Semantics.QUOTED_STRING, sb.ToString()));
                            sb.Length = 0;
                            state = LexanState.QUOTE_2;
                        }
                        else if (c == '\n')
                        {
                            throw new Exception("Invalid Solution File Data: expecting \"");
                        }
                        break;
                    case LexanState.QUOTE_2:
                        sb.Length = 0;
                        state = LexanState.Start_State;
                        continue;




                    #endregion



                    default:
                        throw new Exception(string.Format("Invalid Parse State value: {0} !", (int)state));
                        
                }



                sb.Append(c);
                pointer++;
            }

            return list.ToArray();

        }



        #endregion

        #region Semantics Helper


        int _pointer = -1;

        public bool HasMore
        {
            get
            {

                return tokenData != null ? (_pointer < tokenData.Length-1) : false; 
            }
        }

        public SemanticalData Current
        {
            get
            {

                return tokenData != null && (_pointer >= 0 && _pointer< tokenData.Length) 
                    ? tokenData[_pointer] : null;
            }
        }

        public SemanticalData Next
        {
            get
            {

                return tokenData != null && (_pointer+1 >= 0 && _pointer+1 < tokenData.Length)
                    ? tokenData[_pointer+1] : null;
            }
        }

        public SemanticalData Previous
        {
            get
            {

                return tokenData != null && (_pointer - 1 >= 0 && _pointer - 1 < tokenData.Length)
                    ? tokenData[_pointer - 1] : null;
            }
        }

        public bool MoveNext()
        {
            return tokenData != null ? (++_pointer < tokenData.Length) : false; 
        }


        public bool MovePrevious()
        {
            return tokenData != null ? (--_pointer < tokenData.Length && _pointer >=0) : false;
        }


        public IEnumerator<SemanticalData> GetEnumerator()
        {
            foreach (SemanticalData var in tokenData)
            {
                yield return var;
            }
        }



        public SemanticalData this[int index]
        {
            get { return tokenData[index]; }
        }


        #endregion

        #region Expect Helpers

        private bool expectValue(Semantics expected, Semantics expect)
        {
            return expected == expect;
        }

        private void tryExpectValue(Semantics expected, Semantics expect)
        {
            if (expected != expect)
            {
                throw new Exception(string.Format("Expecting {0} but it is {1}!", expected, expect));
            }
        }

     
        public void Expect(Semantics expect)
        {
            tryExpectValue(expect, Current.Token);
        }

        public void ExpectQuotedOrString()
        {
            if (expectValue(Semantics.STRING_VALUE, Current.Token)
                || expectValue(Semantics.QUOTED_STRING, Current.Token))
            {
                return;
            }
            else
            {
                throw new Exception("String or Quoted String is Expected!");
            }
        }

     

        #endregion









    }
}
